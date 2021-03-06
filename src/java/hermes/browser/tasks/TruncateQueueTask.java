/* 
 * Copyright 2003,2004 Colin Crist
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package hermes.browser.tasks;

import hermes.Domain;
import hermes.Hermes;
import hermes.browser.HermesBrowser;
import hermes.browser.IconCache;
import hermes.browser.actions.BrowserAction;
import hermes.config.DestinationConfig;
import hermes.swing.SwingRunner;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.JMSException;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

/**
 * Truncate a queue.
 * 
 * @author colincrist@hermesjms.com
 * @version $Id: TruncateQueueAction.java,v 1.2 2004/07/30 17:25:14 colincrist
 *          Exp $
 */

public class TruncateQueueTask extends TaskSupport {
	private static final Logger log = Logger.getLogger(TruncateQueueTask.class);

	private DestinationConfig dConfig;
	private Hermes hermes;
	private Collection<String> messageIds;

	private boolean showWarning = true;
	private ProgressMonitor monitor;

	private BrowserAction action;

	public TruncateQueueTask(Hermes hermes, DestinationConfig dConfig, BrowserAction action, boolean showWarning) {
		super(IconCache.getIcon("hermes.messages.delete"));

		this.dConfig = dConfig;
		this.hermes = hermes;
		this.showWarning = showWarning;
		this.action = action;
	}

	public TruncateQueueTask(Hermes hermes, DestinationConfig dConfig, Collection messageIds, BrowserAction action, boolean showWarning) {
		super(IconCache.getIcon("hermes.messages.delete"));

		this.dConfig = dConfig;
		this.hermes = hermes;
		this.messageIds = messageIds;
		this.action = action;
		this.showWarning = showWarning;
	}

	public String getTitle() {
		return (messageIds == null ? "Truncate" : "Delete") + " from " + dConfig.getName();
	}

	public void invoke() throws Exception {
		if (messageIds == null) {
			doTruncate();
		} else {
			doDelete();
		}
		hermes.close();
	}

	private void doTruncate() throws Exception {
		synchronized (this) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					String message = null;

					if (dConfig.getDomain() == Domain.QUEUE.getId()) {
						message = "Are you sure you wish to truncate " + dConfig.getName() + " ?\nAll messages will be deleted from this queue.";
					} else {
						if (dConfig.isDurable()) {
							message = "Are you sure you wish to truncate all messages pending for the durable subscription " + dConfig.getClientID() + " on "
									+ dConfig.getName() + "?";
						}
					}

					if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(), message, "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
						stop();

						notifyStatus("Truncate cancelled");
					}

					synchronized (TruncateQueueTask.this) {
						TruncateQueueTask.this.notify();
					}
				}
			});

			try {
				this.wait();
			} catch (InterruptedException ex) {
				// Nah...
			}
		}

		if (isRunning()) {
			int size = hermes.truncate(dConfig);

			if (size == 1) {
				Hermes.ui.getDefaultMessageSink().add(
						"Message deleted from " + dConfig.getName() + (dConfig.isDurable() ? " durableName=" + dConfig.getClientID() : ""));
			} else {
				Hermes.ui.getDefaultMessageSink().add(
						"Deleted " + size + " messages from " + dConfig.getName() + (dConfig.isDurable() ? " durableName=" + dConfig.getClientID() : ""));
			}
			if (action != null) {
				action.refresh();
			}
		}
	}

	private void doDelete() throws Exception {
		synchronized (this) {
			if (showWarning) {
				SwingRunner.invokeLater(new Runnable() {
					public void run() {
						if (messageIds.size() > 0) {
							if (JOptionPane.showConfirmDialog(HermesBrowser.getBrowser(),
									"Are you sure you wish to delete " + messageIds.size() + ((messageIds.size() > 1) ? " messages" : " message") + " from "
											+ dConfig.getName() + "?", "Warning", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
								stop();

								notifyStatus("Delete from " + dConfig.getName() + " cancelled");
							}

							synchronized (TruncateQueueTask.this) {
								TruncateQueueTask.this.notify();
							}
						} else {
							JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), "No messages selected to delete.", "Cannot Delete",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				});

				try {
					this.wait();
				} catch (InterruptedException ex) {
					// Nah...
				}
			}
		}

		if (isRunning()) {
			final StringBuffer message = new StringBuffer();
			monitor = new ProgressMonitor(HermesBrowser.getBrowser(), "Deleting " + messageIds.size() + ((messageIds.size() == 1) ? " message" : " messages")
					+ " from " + dConfig.getName(), "Connecting...", 0, messageIds.size()) {

			};

			monitor.setMillisToDecideToPopup(50);
			monitor.setMillisToPopup(100);

			try {
				int batchSize = HermesBrowser.getBrowser().getConfig().getDeleteBatch() ;
				ArrayList<String> ids = new ArrayList<String>();

				for (String messageId : messageIds) {
					ids.add(messageId);

					if (ids.size() == batchSize) {
						hermes.delete(dConfig, ids, monitor);
						
						ids.clear();
					}
				}

				if (ids.size() > 0) {
					hermes.delete(dConfig, ids, monitor);	
				}
				
				hermes.commit();
				
				if (action != null) {
					action.refresh();
				}

				hermes.close();

				Hermes.ui.getDefaultMessageSink().add(message.toString());
			} catch (Exception ex) {
				message.append("During delete from ").append(dConfig.getName()).append(": ").append(ex.getMessage());
				log.error(ex.getMessage(), ex);

				try {
					hermes.rollback();
				} catch (JMSException ex2) {
					message.append("\nRollback also failed, probably transport failure.");
					log.error(ex2);
				}

				SwingRunner.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(HermesBrowser.getBrowser(), message.toString(), "Copy Failed.", JOptionPane.OK_OPTION);
					}
				});
			} finally {
				monitor.close();
			}
		}
	}
}