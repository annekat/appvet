/* This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS".  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof including, but
 * not limited to, the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement.
 */
package gov.nist.appvet.gwt.client.gui;

import gov.nist.appvet.gwt.client.GWTService;
import gov.nist.appvet.gwt.client.GWTServiceAsync;
import gov.nist.appvet.gwt.client.gui.dialog.AboutDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.AppUploadDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.LogViewer;
import gov.nist.appvet.gwt.client.gui.dialog.MessageDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.ReportUploadDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.SetAlertDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.ToolAuthParamDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.UserAcctDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.AdminUserListDialogBox;
import gov.nist.appvet.gwt.client.gui.dialog.YesNoConfirmDialog;
import gov.nist.appvet.gwt.client.gui.table.appslist.AppsListPagingDataGrid;
import gov.nist.appvet.gwt.shared.AppInfoGwt;
import gov.nist.appvet.gwt.shared.AppsListGwt;
import gov.nist.appvet.gwt.shared.ConfigInfoGwt;
import gov.nist.appvet.gwt.shared.ServerPacket;
import gov.nist.appvet.gwt.shared.SystemAlert;
import gov.nist.appvet.gwt.shared.SystemAlertType;
import gov.nist.appvet.gwt.shared.ToolInfoGwt;
import gov.nist.appvet.gwt.shared.ToolStatusGwt;
import gov.nist.appvet.shared.all.AppStatus;
import gov.nist.appvet.shared.all.AppVetParameter;
import gov.nist.appvet.shared.all.AppVetServletCommand;
import gov.nist.appvet.shared.all.DeviceOS;
import gov.nist.appvet.shared.all.Role;
import gov.nist.appvet.shared.all.ToolType;
import gov.nist.appvet.shared.all.UserInfo;
import gov.nist.appvet.shared.all.Validate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author steveq@nist.gov
 */
@SuppressWarnings("deprecation")
public class AppVetPanel extends DockLayoutPanel {

	// See appvet.gwt.xml
	private final int NUM_APPS_SHOW_REFRESH_WARNING = 0;
	private Logger log = Logger.getLogger("AppVetPanel");
	private SingleSelectionModel<AppInfoGwt> appSelectionModel = null;
	private long MAX_SESSION_IDLE_DURATION = 0;
	private int POLLING_INTERVAL = 0;
	private final GWTServiceAsync appVetServiceAsync = GWT
			.create(GWTService.class);
	private HTML appInfoName = null;
	private HTML appInfoPackage = null;
	private HTML appInfoVersion = null;
	private HTML appStatusInfo = null;
	private Image appInfoIcon = null;
	private HTML toolResultsHtml = null;
	private AppsListPagingDataGrid<AppInfoGwt> appsListTable = null;
	private Date lastAppsListUpdate = null;
	private UserInfo userInfo = null;
	private String userName = null;
	private PushButton viewAllButton = null;
	private PushButton deleteButton = null;
	private PushButton downloadReportsButton = null;
	private PushButton uploadReportButton = null;
	private PushButton downloadAppButton = null;
	private PushButton logButton = null;
	private List<AppInfoGwt> allApps = null;
	private TextBox searchTextBox = null;
	private String sessionId = null;
	private Date sessionExpiration = null;
	private Timer pollingTimer = null;
	private Timer warningTimer = null;
	private HorizontalPanel appsListButtonPanel = null;
	private SimplePanel rightCenterPanel = null;
	private AppUploadDialogBox appUploadDialogBox = null;
	private MessageDialogBox messageDialogBox = null;
	private AboutDialogBox aboutDialogBox = null;
	private AdminUserListDialogBox usersDialogBox = null;
	private YesNoConfirmDialog deleteConfirmDialogBox = null;
	private ReportUploadDialogBox reportUploadDialogBox = null;
	private UserAcctDialogBox userAcctDialogBox = null;
	public final InlineHTML statusMessageHtml = new InlineHTML("");
	private String SERVLET_URL = null;
	private ArrayList<ToolInfoGwt> tools = null;
	private HTML appsLabelHtml = null;
	private SimplePanel centerPanel = null;
	private double NORTH_PANEL_HEIGHT = 65.0;
	private double SOUTH_PANEL_HEIGHT = 47.0;
	private boolean searchMode = false;
	private MenuItem userMenuItem = null;
	public boolean timeoutWarningMessage = false;
	public String documentationURL = null;
	public boolean ssoActive = false;
	public String ssoLogoutURL = null;
	public boolean keepApps = false;

	class AppListHandler implements SelectionChangeEvent.Handler {
		ConfigInfoGwt configInfo = null;
		AppVetPanel appVetPanel = null;

		public AppListHandler(AppVetPanel appVetPanel, ConfigInfoGwt configInfo) {
			this.appVetPanel = appVetPanel;
			this.configInfo = configInfo;
		}

		@Override
		public void onSelectionChange(SelectionChangeEvent event) {
			final AppInfoGwt selectedApp = appSelectionModel
					.getSelectedObject();
			displaySelectedAppInfo(selectedApp);
		}
	}

	class AppUploadFormHandler implements FormHandler {
		AppUploadDialogBox appUploadDialog = null;

		// String apkFileName = null;

		public AppUploadFormHandler(AppUploadDialogBox appUploadDialog) {
			this.appUploadDialog = appUploadDialog;
		}

		@Override
		@Deprecated
		public void onSubmit(FormSubmitEvent event) {
		}

		@Override
		@Deprecated
		public void onSubmitComplete(FormSubmitCompleteEvent event) {
			String appFileName = appUploadDialogBox.fileUpload.getFilename();
			appUploadDialog.mainLabel.setText("");
			appUploadDialog.statusLabel.setText("");
			killDialogBox(appUploadDialog);

			showMessageDialog("App Submission", "App \"" + appFileName
					+ "\" was successfully uploaded.", false);
		}
	}

	class ReportUploadFormHandler implements FormHandler {
		ReportUploadDialogBox reportUploadDialogBox = null;
		String username = null;
		String appid = null;
		DeviceOS appOs = null;
		AppInfoGwt selected = null;

		public ReportUploadFormHandler(
				ReportUploadDialogBox reportUploadDialogBox, String username,
				AppInfoGwt selectedApp) {
			this.reportUploadDialogBox = reportUploadDialogBox;
			this.selected = selectedApp;
			this.username = username;
			this.appid = selectedApp.appId;
			this.appOs = selectedApp.os;
		}

		@Override
		@Deprecated
		public void onSubmit(FormSubmitEvent event) {
			String reportFileName = reportUploadDialogBox.fileUpload
					.getFilename();
			int selectedToolIndex = reportUploadDialogBox.toolNamesComboBox
					.getSelectedIndex();
			String selectedToolName = reportUploadDialogBox.toolNamesComboBox
					.getValue(selectedToolIndex);
			if (reportFileName.length() == 0) {
				showMessageDialog("Report Submission Error",
						"No file selected", true);
				event.setCancelled(true);
			} else if (!Validate.isLegalFileName(reportFileName)) {
				showMessageDialog("Report Submission Error", "File \""
						+ reportFileName + "\" contains an illegal character.",
						true);
				event.setCancelled(true);
			} else if (!validReportFileName(selectedToolName, reportFileName,
					tools, appOs)) {
				event.setCancelled(true);
			} else {
				reportUploadDialogBox.cancelButton.setEnabled(false);
				reportUploadDialogBox.submitButton.setEnabled(false);
				reportUploadDialogBox.statusLabel.setText("Uploading "
						+ reportFileName + "...");
			}
		}

		@Override
		@Deprecated
		public void onSubmitComplete(FormSubmitCompleteEvent event) {
			reportUploadDialogBox.statusLabel.setText("");
			killDialogBox(reportUploadDialogBox);
		}
	}

	public void killDialogBox(DialogBox dialogBox) {
		if (dialogBox != null) {
			dialogBox.hide();
			dialogBox = null;
		}
	}

	public void logoutSSO() {
		// Cancel poller
		pollingTimer.cancel();

		// Close any open dialog boxes
		killDialogBox(appUploadDialogBox);
		killDialogBox(messageDialogBox);
		killDialogBox(aboutDialogBox);
		killDialogBox(usersDialogBox);
		killDialogBox(deleteConfirmDialogBox);
		killDialogBox(reportUploadDialogBox);
		killDialogBox(userAcctDialogBox);

		// Redirect to the SSO logout URL
		Window.Location.assign(ssoLogoutURL);
		System.gc();
	}

	public void logoutNonSSO() {
		// Cancel poller
		pollingTimer.cancel();

		// Close any open dialog boxes
		killDialogBox(appUploadDialogBox);
		killDialogBox(messageDialogBox);
		killDialogBox(aboutDialogBox);
		killDialogBox(usersDialogBox);
		killDialogBox(deleteConfirmDialogBox);
		killDialogBox(reportUploadDialogBox);
		killDialogBox(userAcctDialogBox);

		// Go back to AppVet login screen
		final LoginPanel loginPanel = new LoginPanel();
		// loginPanel.setTitle("Login panel");
		final RootLayoutPanel rootLayoutPanel = RootLayoutPanel.get();
		// rootLayoutPanel.setTitle("Root panel");
		rootLayoutPanel.clear();
		rootLayoutPanel.add(loginPanel);
		// Clean up
		System.gc();
	}

	public boolean userInfoIsValid(UserInfo userInfo, boolean ssoActive) {

		if (!Validate.isValidUserName(userInfo.getUserName())) {
			showMessageDialog("Account Setting Error", "Invalid username", true);
			return false;
		}

		if (!Validate.isAlpha(userInfo.getLastName())) {
			showMessageDialog("Account Setting Error", "Invalid last name",
					true);
			return false;
		}

		if (!Validate.isAlpha(userInfo.getFirstName())) {
			showMessageDialog("Account Setting Error", "Invalid first name",
					true);
			return false;
		}

		if (!Validate.isValidEmail(userInfo.getEmail())) {
			showMessageDialog("Account Setting Error", "Invalid email", true);
			return false;
		}

		if (!ssoActive) {
			// Password is required for NON-SSO mode
			String password = userInfo.getPassword();
			String passwordAgain = userInfo.getPasswordAgain();
			if (password != null && !password.isEmpty()
					&& passwordAgain != null && !passwordAgain.isEmpty()) {
				if (!Validate.isValidPassword(password)) {
					showMessageDialog("Account Setting Error",
							"Invalid password", true);
					return false;
				}
				if (!password.equals(passwordAgain)) {
					showMessageDialog("Account Setting Error",
							"Passwords do not match", true);
					return false;
				}
			} else {
				showMessageDialog("Account Setting Error",
						"Password is empty or null", true);
				return false;
			}
		} else {
			// SSO is active so we ignore password fields (since passwords
			// are handled by the organization's SSO environment. Do nothing.
		}
		return true;
	}

	public void showMessageDialog(String windowTitle, String message,
			boolean isError) {
		messageDialogBox = new MessageDialogBox(message, isError);
		messageDialogBox.setText(windowTitle);
		messageDialogBox.center();
		messageDialogBox.closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				killDialogBox(messageDialogBox);
			}
		});
	}

	public void showTimeoutDialog(final long diff) {
		killDialogBox(messageDialogBox);
		timeoutWarningMessage = true;
		messageDialogBox = new MessageDialogBox(
				"Your AppVet session will expire in less than 60 seconds. Please select OK to continue using AppVet.",
				false);
		messageDialogBox.setText("AppVet Timeout Warning");
		messageDialogBox.center();
		messageDialogBox.closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				killDialogBox(messageDialogBox);

				if (diff <= 0) {
					// Didn't click within the 60s alert period, so expire
					// pollingTimer.cancel();
					removeSession(true);
				} else {
					sessionExpiration = new Date(System.currentTimeMillis()
							+ MAX_SESSION_IDLE_DURATION);
					timeoutWarningMessage = false;
				}
			}
		});
	}

	public boolean validReportFileName(String selectedToolName,
			String uploadedReportFileName, ArrayList<ToolInfoGwt> tools,
			DeviceOS appOs) {
		String selectedToolRequiredFileType = null;
		for (int i = 0; i < tools.size(); i++) {
			ToolInfoGwt tool = tools.get(i);
			String toolOs = tool.getOs();
			String toolName = tool.getName();
			if (selectedToolName.equals(toolName)
					&& toolOs.equals(appOs.name())) {
				selectedToolRequiredFileType = tool.getReportFileType();
				break;
			} else {
				selectedToolRequiredFileType = "";
			}
		}

		final String uploadedReportFileNameLowercase = uploadedReportFileName
				.toLowerCase();
		if (selectedToolRequiredFileType == null) {
			log.severe("selectedToolRequiredFileType is null");
			return false;
		}
		final String selectedToolRequiredFileTypeLowercase = selectedToolRequiredFileType
				.toLowerCase();

		if (selectedToolRequiredFileTypeLowercase.endsWith("html")) {
			if (!uploadedReportFileNameLowercase.endsWith("html")) {
				showMessageDialog("Report Submission Error", selectedToolName
						+ " reports must be HTML files.", true);
				return false;
			}
		} else if (selectedToolRequiredFileTypeLowercase.endsWith("pdf")) {
			if (!uploadedReportFileNameLowercase.endsWith("pdf")) {
				showMessageDialog("Report Submission Error", selectedToolName
						+ " reports must be PDF files.", true);
				return false;
			}
		} else if (selectedToolRequiredFileTypeLowercase.endsWith("txt")) {
			if (!uploadedReportFileNameLowercase.endsWith("txt")) {
				showMessageDialog("Report Submission Error", selectedToolName
						+ " reports must be TXT files.", true);
				return false;
			}
		} else if (selectedToolRequiredFileTypeLowercase.endsWith("rtf")) {
			if (!uploadedReportFileNameLowercase.endsWith("rtf")) {
				showMessageDialog("Report Submission Error", selectedToolName
						+ " reports must be RTF files.", true);
				return false;
			}
		} else if (selectedToolRequiredFileTypeLowercase.endsWith("xml")) {
			if (!uploadedReportFileNameLowercase.endsWith("xml")) {
				showMessageDialog("Report Submission Error", selectedToolName
						+ " reports must be XML files.", true);
				return false;
			}
		}

		return true;
	}

	public AppVetPanel(final ConfigInfoGwt configInfo, AppsListGwt initialApps) {
		super(Unit.PX);

		Window.addResizeHandler(new ResizeHandler() {
			Timer resizeTimer = new Timer() {
				@Override
				public void run() {
					adjustComponentSizes();
				}
			};

			@Override
			public void onResize(ResizeEvent event) {
				resizeTimer.cancel();
				resizeTimer.schedule(250);
			}
		});

		userInfo = configInfo.getUserInfo();
		userName = userInfo.getUserName();
		lastAppsListUpdate = initialApps.appsLastChecked;
		allApps = initialApps.apps;

		sinkEvents(Event.ONCLICK);
		sessionId = configInfo.getSessionId();
		sessionExpiration = configInfo.getSessionExpiration();
		MAX_SESSION_IDLE_DURATION = configInfo.getMaxIdleTime();
		POLLING_INTERVAL = configInfo.getUpdatesDelay();
		setSize("", "");
		SERVLET_URL = configInfo.getAppVetServletUrl();
		appSelectionModel = new SingleSelectionModel<AppInfoGwt>();
		appSelectionModel.addSelectionChangeHandler(new AppListHandler(this,
				configInfo));

		tools = configInfo.getTools();
		documentationURL = configInfo.getDocumentationURL();
		ssoActive = configInfo.getSSOActive();
		ssoLogoutURL = configInfo.getSsoLogoutURL();
		String orgLogoAltText = configInfo.getOrgLogoAltText();
		keepApps = configInfo.keepApps();

		final MenuBar adminMenuBar = new MenuBar(true);
		adminMenuBar.setStyleName("adminMenuBar");

		// Set tab-able for 508 compliance
		Roles.getMenubarRole().setTabindexExtraAttribute(
				adminMenuBar.getElement(), -1);
		adminMenuBar.setFocusOnHoverEnabled(true);

		// Admin menubar
		final MenuItem adminMenuItem = new MenuItem("Admin", true, adminMenuBar);
		adminMenuItem.setTitle("Admin");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				adminMenuItem.getElement(), 0);

		final MenuItem usersMenuItem = new MenuItem("Add/Edit Users", false,
				new Command() {
					@Override
					public void execute() {
						usersDialogBox = new AdminUserListDialogBox(configInfo,
								ssoActive);
						usersDialogBox.setText("Users");
						usersDialogBox.center();
						usersDialogBox.doneButton
								.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										killDialogBox(usersDialogBox);
									}
								});
					}
				});
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				usersMenuItem.getElement(), 0);
		usersMenuItem.setHTML("Add/Edit Users");
		adminMenuBar.addItem(usersMenuItem);
		usersMenuItem.setStyleName("adminSubMenuItem");

		MenuItemSeparator separator_1 = new MenuItemSeparator();
		adminMenuBar.addSeparator(separator_1);
		separator_1.setSize("100%", "1px");

		final MenuItem clearAlertMessageMenuItem = new MenuItem(
				"Clear Status Message", false, new Command() {
					@Override
					public void execute() {
						clearAlertMessage(userInfo.getUserName());
					}
				});
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				clearAlertMessageMenuItem.getElement(), 0);
		clearAlertMessageMenuItem.setHTML("Clear Status Message");
		adminMenuBar.addItem(clearAlertMessageMenuItem);
		clearAlertMessageMenuItem.setStyleName("adminSubMenuItem");

		final MenuItem setAlertMenuItem = new MenuItem("Set Status Message",
				false, new Command() {
					@Override
					public void execute() {
						final SetAlertDialogBox setAlertDialogBox = new SetAlertDialogBox();
						setAlertDialogBox.setText("Set Alert Message");
						setAlertDialogBox.center();
						setAlertDialogBox.cancelButton
								.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										killDialogBox(setAlertDialogBox);
										return;
									}
								});
						setAlertDialogBox.okButton
								.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										killDialogBox(setAlertDialogBox);
										SystemAlertType alertType = null;
										if (setAlertDialogBox.alertNormalRadioButton
												.getValue())
											alertType = SystemAlertType.NORMAL;
										else if (setAlertDialogBox.alertWarningRadioButton
												.getValue())
											alertType = SystemAlertType.WARNING;
										else if (setAlertDialogBox.alertCriticalRadioButton
												.getValue())
											alertType = SystemAlertType.CRITICAL;

										String alertMessage = setAlertDialogBox.alertTextArea
												.getText();
										if (alertMessage == null
												|| alertMessage.isEmpty()) {
											showMessageDialog(
													"AppVet Error",
													"Alert message cannot be empty.",
													true);
										}

										setAlertMessage(userInfo.getUserName(),
												alertType, alertMessage);
									}
								});
					}
				});
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				setAlertMenuItem.getElement(), 0);

		adminMenuBar.addItem(setAlertMenuItem);
		setAlertMenuItem.setStyleName("adminSubMenuItem");

		MenuItemSeparator separator_2 = new MenuItemSeparator();
		adminMenuBar.addSeparator(separator_2);
		separator_2.setSize("100%", "1px");

		final MenuItem mntmAppVetLog = new MenuItem("View Log", false,
				new Command() {
					@Override
					public void execute() {
						showAppVetLog();
					}
				});
		mntmAppVetLog.setHTML("View Log");
		adminMenuBar.addItem(mntmAppVetLog);
		mntmAppVetLog.setStyleName("adminSubMenuItem");

		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				mntmAppVetLog.getElement(), 0);

		final MenuItem clearAppVetLogMenuItem = new MenuItem("Clear Log",
				false, new Command() {
					@Override
					public void execute() {
						final YesNoConfirmDialog clearLogDialogBox = new YesNoConfirmDialog(
								"<p align=\"center\">\r\nAre you sure you want to clear the AppVet log?\r\n</p>");
						clearLogDialogBox.setText("Confirm Clear");
						clearLogDialogBox.center();
						clearLogDialogBox.cancelButton.setFocus(true);
						clearLogDialogBox.cancelButton
								.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										killDialogBox(clearLogDialogBox);
										return;
									}
								});
						clearLogDialogBox.okButton
								.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										killDialogBox(clearLogDialogBox);
										clearLog();
									}
								});
					}
				});
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				clearAppVetLogMenuItem.getElement(), 0);
		adminMenuBar.addItem(clearAppVetLogMenuItem);
		clearAppVetLogMenuItem.setStyleName("adminSubMenuItem");

		final MenuItem downloadAppVetLogMenuItem = new MenuItem("Download Log",
				false, new Command() {
					@Override
					public void execute() {
						final String dateString = "?nocache"
								+ new Date().getTime();
						final String url = SERVLET_URL + dateString + "&"
								+ AppVetParameter.COMMAND.value + "="
								+ AppVetServletCommand.DOWNLOAD_LOG.name()
								+ "&" + AppVetParameter.SESSIONID.value + "="
								+ sessionId;
						Window.open(url, "_self", "");
					}
				});
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				downloadAppVetLogMenuItem.getElement(), 0);
		downloadAppVetLogMenuItem.setHTML("Download Log");
		adminMenuBar.addItem(downloadAppVetLogMenuItem);
		downloadAppVetLogMenuItem.setStyleName("adminSubMenuItem");

		adminMenuItem.setStyleName("adminMenuItem");

		adminMenuItem
				.setHTML("<img src=\"images/icon-gear.png\" width=\"16px\" height=\"16px\" alt=\"Admin\">");

		final MenuBar appVetMenuBar = new MenuBar(false);

		// REMOVE THE FOLLOWING ONLY FOR TESTING
		// appVetMenuBar.addItem(adminMenuItem);

		Role role;
		try {
			role = Role.getRole(userInfo.getRoleAndOrgMembership());
			if (role == Role.ADMIN) {
				appVetMenuBar.addItem(adminMenuItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!keepApps) {
			// Hide download app button if KEEP_APPS is false
			downloadAppButton.setVisible(false);
		}

		/*
		 * The appsListTable must be set to a height in pixels (not percent) and
		 * must be adjusted during run-time using the resizeComponent() method.
		 */
		pollServer(userName);

		SimplePanel northPanel = new SimplePanel();
		addNorth(northPanel, 54.0);
		northPanel.setHeight("");
		final VerticalPanel northAppVetPanel = new VerticalPanel();
		northPanel.setWidget(northAppVetPanel);
		northAppVetPanel
				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		northAppVetPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		northAppVetPanel.setSize("100%", "");
		final HorizontalPanel topBannerPanel = new HorizontalPanel();
		topBannerPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		topBannerPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		topBannerPanel.setStyleName("mainBanner");
		northAppVetPanel.add(topBannerPanel);
		northAppVetPanel.setCellVerticalAlignment(topBannerPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		topBannerPanel.setSize("100%", "");
		northAppVetPanel.setCellWidth(topBannerPanel, "100%");

		Image appvetLogoMain = new Image(
				"../appvet_images/appvet_logo_main.png");
		appvetLogoMain.setAltText("AppVet");
		topBannerPanel.add(appvetLogoMain);
		appvetLogoMain.setWidth("200px");
		appvetLogoMain.setHeight("25px");
		topBannerPanel.setCellVerticalAlignment(appvetLogoMain,
				HasVerticalAlignment.ALIGN_BOTTOM);
		final HorizontalPanel searchPanel = new HorizontalPanel();
		searchPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		topBannerPanel.add(searchPanel);
		searchPanel.setWidth("");
		topBannerPanel.setCellWidth(searchPanel, "100%");
		topBannerPanel.setCellHorizontalAlignment(searchPanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		topBannerPanel.setCellVerticalAlignment(searchPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		searchTextBox = new TextBox();
		searchTextBox.setText("Search");
		searchTextBox.setStyleName("searchTextBox");
		searchTextBox.setTitle("Search by app ID, name, release kit, etc.");
		searchTextBox.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				searchTextBox.setText("");
			}
		});

		searchTextBox.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event_) {
				final boolean enterPressed = KeyCodes.KEY_ENTER == event_
						.getNativeEvent().getKeyCode();
				final String searchString = searchTextBox.getText();
				if (enterPressed) {
					final int numFound = search();
					if (numFound > 0) {
						final SafeHtmlBuilder sb = new SafeHtmlBuilder();
						sb.appendHtmlConstant("<h3>Found " + numFound
								+ " results for \"" + searchString + "\"</h3>");
						appsLabelHtml.setHTML(sb.toSafeHtml());
					}
				}
			}
		});

		searchTextBox.setSize("240px", "15px");
		searchPanel.add(searchTextBox);
		searchPanel.setCellVerticalAlignment(searchTextBox,
				HasVerticalAlignment.ALIGN_MIDDLE);
		final PushButton searchButton = new PushButton("Search");
		searchButton.setStyleName("searchButton");
		searchButton.setTitle("Search by app ID, name, release kit, etc.");
		searchButton.setSize("", "");
		searchButton
				.setHTML("<img width=\"18px\" height=\"18px\" src=\"images/icon-search.png\" alt=\"search\" />");
		searchButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final String searchString = searchTextBox.getText();
				final int numFound = search();
				if (numFound > 0) {
					final SafeHtmlBuilder sb = new SafeHtmlBuilder();
					sb.appendHtmlConstant("<h3>Found " + numFound
							+ " results for \"" + searchString + "\"</h3>");
					appsLabelHtml.setHTML(sb.toSafeHtml());
				}
			}
		});
		searchPanel.add(searchButton);
		searchPanel.setCellHorizontalAlignment(searchButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		searchPanel.setCellVerticalAlignment(searchButton,
				HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel menuBarPanel = new HorizontalPanel();
		menuBarPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		menuBarPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		topBannerPanel.add(menuBarPanel);
		topBannerPanel.setCellVerticalAlignment(menuBarPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		topBannerPanel.setCellHorizontalAlignment(menuBarPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);

		Roles.getMenubarRole().setTabindexExtraAttribute(
				appVetMenuBar.getElement(), -1);
		menuBarPanel.add(appVetMenuBar);
		menuBarPanel.setCellVerticalAlignment(appVetMenuBar,
				HasVerticalAlignment.ALIGN_MIDDLE);
		menuBarPanel.setCellHorizontalAlignment(appVetMenuBar,
				HasHorizontalAlignment.ALIGN_RIGHT);
		appVetMenuBar.setStyleName("gwt-MenuBar");
		appVetMenuBar.setAutoOpen(false);
		appVetMenuBar.setSize("250px", "");
		appVetMenuBar.setAnimationEnabled(false);
		appVetMenuBar.setFocusOnHoverEnabled(false);

		final MenuBar userMenuBar = new MenuBar(true);
		userMenuBar.setStyleName("userMenuBar");
		userMenuBar.setFocusOnHoverEnabled(false);
		// Set tab-able for 508 compliance
		Roles.getMenubarRole().setTabindexExtraAttribute(
				userMenuBar.getElement(), -1);

		userMenuItem = new MenuItem("User", true, userMenuBar);
		userMenuItem.setStyleName("userMenuItem");
		userMenuItem.setTitle("User Preferences");
		userMenuItem
				.setHTML("<img src=\"images/icon-user.png\" width=\"16px\" height=\"16px\" alt=\"User Preferences\">");
		userMenuBar.setHeight("");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				userMenuItem.getElement(), 0);

		final MenuItem accountSettingsMenuItem = new MenuItem(
				"Account Settings", false, new Command() {
					@Override
					public void execute() {
						openUserAccount(configInfo);
					}
				});
		accountSettingsMenuItem.setStyleName("userSubMenuItem");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				accountSettingsMenuItem.getElement(), 0);
		userMenuBar.addItem(accountSettingsMenuItem);

		final MenuItem toolCredentialsMenuItem = new MenuItem(
				"Tool Credentials", false, new Command() {
					@Override
					public void execute() {
						openToolCredentials(configInfo);
					}
				});
		toolCredentialsMenuItem.setStyleName("userSubMenuItem");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				toolCredentialsMenuItem.getElement(), 0);

		userMenuBar.addItem(toolCredentialsMenuItem);
		accountSettingsMenuItem.setHeight("");

		final MenuItem myAppsMenuItem = new MenuItem("My Apps", false,
				new Command() {
					@Override
					public void execute() {
						searchTextBox.setText(userInfo.getUserName());
						final int numFound = search();
						if (numFound > 0) {
							final SafeHtmlBuilder sb = new SafeHtmlBuilder();
							sb.appendHtmlConstant("<h3>My Apps</h3>");
							appsLabelHtml.setHTML(sb.toSafeHtml());
						}
					}
				});
		myAppsMenuItem.setStyleName("userSubMenuItem");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				myAppsMenuItem.getElement(), 0);
		userMenuBar.addItem(myAppsMenuItem);
		myAppsMenuItem.setHeight("");

		final MenuItemSeparator separator = new MenuItemSeparator();
		userMenuBar.addSeparator(separator);
		separator.setSize("100%", "1px");
		final MenuItem logoutMenuItem = new MenuItem("Logout", false,
				new Command() {
					@Override
					public void execute() {
						removeSession(false);
					}
				});
		logoutMenuItem.setStyleName("userSubMenuItem");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				logoutMenuItem.getElement(), 0);
		userMenuBar.addItem(logoutMenuItem);
		logoutMenuItem.setHeight("");

		appVetMenuBar.addItem(userMenuItem);
		userMenuItem.setHeight("");

		final MenuBar helpMenuBar = new MenuBar(true);
		helpMenuBar.setStyleName("helpMenuBar");
		// Set tab-able for 508 compliance
		Roles.getMenubarRole().setTabindexExtraAttribute(
				helpMenuBar.getElement(), -1);
		helpMenuBar.setFocusOnHoverEnabled(false);

		final MenuItem helpMenuItem = new MenuItem("Help", true, helpMenuBar);
		helpMenuItem.setTitle("Help");
		helpMenuItem
				.setHTML("<img src=\"images/icon-white-question-mark.png\"  width=\"16px\" height=\"16px\" alt=\"Settings\">");
		helpMenuItem.setStyleName("helpMenuItem");
		helpMenuBar.setHeight("");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				helpMenuItem.getElement(), 0);

		final MenuItem aboutMenuItem = new MenuItem("About", false,
				new Command() {
					@Override
					public void execute() {
						aboutDialogBox = new AboutDialogBox(configInfo
								.getAppVetVersion());
						//aboutDialogBox.setText("About");
						aboutDialogBox.center();
						aboutDialogBox.closeButton
								.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										killDialogBox(aboutDialogBox);
									}
								});
					}
				});
		aboutMenuItem.setStyleName("helpSubMenuItem");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				aboutMenuItem.getElement(), 0);

		final MenuItem documentationMenuItem = new MenuItem("Documentation",
				false, new Command() {
					@Override
					public void execute() {
						Window.open(documentationURL, "_blank", null);
					}
				});
		documentationMenuItem.setStyleName("helpSubMenuItem");
		// Set tab-able for 508 compliance
		Roles.getMenuitemRole().setTabindexExtraAttribute(
				documentationMenuItem.getElement(), 0);

		helpMenuBar.addItem(documentationMenuItem);
		documentationMenuItem.setHeight("");

		appVetMenuBar.addItem(helpMenuItem);
		helpMenuItem.setHeight("");
		helpMenuBar.addItem(aboutMenuItem);
		aboutMenuItem.setHeight("");

		final HorizontalPanel statusMessagePanel = new HorizontalPanel();
		statusMessagePanel
				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		statusMessagePanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		northAppVetPanel.add(statusMessagePanel);
		northAppVetPanel.setCellVerticalAlignment(statusMessagePanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		northAppVetPanel.setCellHorizontalAlignment(statusMessagePanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		statusMessagePanel.setSize("100%", "");
		northAppVetPanel.setCellWidth(statusMessagePanel, "100%");

		statusMessagePanel.add(statusMessageHtml);
		statusMessagePanel.setCellHeight(statusMessageHtml, "25px");
		statusMessagePanel.setCellVerticalAlignment(statusMessageHtml,
				HasVerticalAlignment.ALIGN_MIDDLE);
		statusMessagePanel.setCellHorizontalAlignment(statusMessageHtml,
				HasHorizontalAlignment.ALIGN_CENTER);
		statusMessagePanel.setCellWidth(statusMessageHtml, "100%");
		statusMessageHtml.setStyleName("statusMessage");
		statusMessageHtml
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		statusMessageHtml.setSize("90%", "28px");

		centerPanel = new SimplePanel();
		add(centerPanel);
		centerPanel.setHeight("");

		HorizontalPanel mainHorizontalPanel = new HorizontalPanel();
		centerPanel.setWidget(mainHorizontalPanel);
		mainHorizontalPanel.setSize("100%", "100%");
		appsListTable = new AppsListPagingDataGrid<AppInfoGwt>();
		appsListTable.pager.setHeight("");
		appsListTable.dataGrid
				.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
		appsListTable.dataGrid.setFocus(false);
		appsListTable.setPageSize(configInfo.getNumRowsAppsList());
		appsListTable.dataGrid.setStyleName("dataGrid");
		appsListTable.dataGrid.setSize("100%", "");
		appsListTable.setDataList(initialApps.apps);
		appsListTable.setSize("100%", "200px");
		appsListTable.dataGrid.setSelectionModel(appSelectionModel);
		final SimplePanel leftCenterPanel = new SimplePanel();
		leftCenterPanel.setStyleName("leftCenterPanel");
		mainHorizontalPanel.add(leftCenterPanel);
		mainHorizontalPanel.setCellWidth(leftCenterPanel, "100%");
		leftCenterPanel.setWidth("");
		final DockPanel dockPanel_1 = new DockPanel();
		dockPanel_1.add(appsListTable, DockPanel.CENTER);
		dockPanel_1.setCellWidth(appsListTable, "100%");
		dockPanel_1.setCellHorizontalAlignment(appsListTable,
				HasHorizontalAlignment.ALIGN_CENTER);
		dockPanel_1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		dockPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		leftCenterPanel.setWidget(dockPanel_1);
		dockPanel_1.setSize("100%", "");
		appsListButtonPanel = new HorizontalPanel();
		appsListButtonPanel
				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		dockPanel_1.add(appsListButtonPanel, DockPanel.NORTH);
		dockPanel_1.setCellHorizontalAlignment(appsListButtonPanel,
				HasHorizontalAlignment.ALIGN_CENTER);
		dockPanel_1.setCellWidth(appsListButtonPanel, "100%");
		dockPanel_1.setCellVerticalAlignment(appsListButtonPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.setSize("100%", "");

		appsLabelHtml = new HTML("<h3>Apps</h3>", true);
		appsLabelHtml.setStyleName("appsLabel");
		appsListButtonPanel.add(appsLabelHtml);
		final HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.add(horizontalPanel);
		appsListButtonPanel.setCellWidth(horizontalPanel, "50%");
		appsListButtonPanel.setCellVerticalAlignment(horizontalPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.setCellHorizontalAlignment(horizontalPanel,
				HasHorizontalAlignment.ALIGN_RIGHT);
		horizontalPanel.setSize("", "");
		final PushButton submitButton = new PushButton("Upload App");
		submitButton.setStyleName("greenAppUploadButton shadow");

		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				appUploadDialogBox = new AppUploadDialogBox(sessionId,
						SERVLET_URL);
				appUploadDialogBox.setText("Submit App");
				appUploadDialogBox.center();
				appUploadDialogBox.cancelButton
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								killDialogBox(appUploadDialogBox);
							}
						});
				appUploadDialogBox.uploadAppFileForm
						.addFormHandler(new AppUploadFormHandler(
								appUploadDialogBox));
				appUploadDialogBox.submitButton
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (appUploadDialogBox.fileUpload.getFilename()
										.isEmpty()) {
									showMessageDialog("Submit App File",
											"No app file selected.", true);
									return;
								}
								appUploadDialogBox.cancelButton
										.setEnabled(false);
								appUploadDialogBox.submitButton
										.setEnabled(false);
								String fileName = appUploadDialogBox.fileUpload
										.getFilename();
								appUploadDialogBox.statusLabel
										.setText("Uploading " + fileName
												+ "...");
								appUploadDialogBox.uploadAppFileForm.submit();
							}
						});
			}
		});
		viewAllButton = new PushButton("View All");
		// viewAllButton.setStyleName("appvetButton shadow");
		viewAllButton.setStyleName("blueButton shadow");
		// viewAllButton.setHTML("<img width=\"100px\" src=\"images/icon-view-all.png\" alt=\"View All Apps\" />");
		viewAllButton.setHTML("View All");
		viewAllButton.setTitle("View All Apps");
		viewAllButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				searchMode = false;
				setAllApps();
				viewAllButton.setVisible(false);
			}
		});
		horizontalPanel.add(viewAllButton);
		horizontalPanel.setCellHorizontalAlignment(viewAllButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.setCellVerticalAlignment(viewAllButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		viewAllButton.setSize("120px", "18px");
		viewAllButton.setVisible(false);
		horizontalPanel.add(submitButton);
		horizontalPanel.setCellVerticalAlignment(submitButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.setCellHorizontalAlignment(submitButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		submitButton.setSize("120px", "18px");

		downloadAppButton = new PushButton("Download App");
		downloadAppButton.setEnabled(false);
		rightCenterPanel = new SimplePanel();
		rightCenterPanel.setStyleName("rightCenterPanel");
		mainHorizontalPanel.add(rightCenterPanel);
		rightCenterPanel.setWidth("570px");
		mainHorizontalPanel.setCellWidth(rightCenterPanel, "570px");
		final VerticalPanel appInfoVerticalPanel = new VerticalPanel();
		rightCenterPanel.setWidget(appInfoVerticalPanel);
		appInfoVerticalPanel.setSize("100%", "");
		final HorizontalPanel appInfoPanel = new HorizontalPanel();
		appInfoPanel.setStyleName("iconPanel");
		appInfoVerticalPanel.add(appInfoPanel);
		appInfoVerticalPanel.setCellWidth(appInfoPanel, "100%");
		appInfoPanel.setSize("", "");
		appInfoIcon = new Image("");
		appInfoIcon.setVisible(false);
		appInfoIcon.setAltText("");
		appInfoPanel.add(appInfoIcon);
		appInfoPanel.setCellVerticalAlignment(appInfoIcon,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appInfoIcon.setSize("82px", "82px");
		final VerticalPanel verticalPanel = new VerticalPanel();
		appInfoPanel.add(verticalPanel);
		verticalPanel.setHeight("82px");
		appInfoName = new HTML("App Name", false);
		appInfoName.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		verticalPanel.add(appInfoName);
		appInfoName.setSize("500px", "33px");
		appInfoPanel.setCellVerticalAlignment(appInfoName,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appInfoPackage = new HTML("Package: ", true);
		appInfoPackage
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		appInfoPackage.setStyleName("appInfoVersion");
		verticalPanel.add(appInfoPackage);
		appInfoPackage.setSize("500px", "14px");
		appInfoVersion = new HTML("Version: ", true);
		appInfoVersion
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		appInfoVersion.setStyleName("appInfoVersion");
		verticalPanel.add(appInfoVersion);
		appInfoVersion.setSize("500px", "14px");
		
		appStatusInfo = new HTML("Status: ", true);
		appStatusInfo.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		appStatusInfo.setStyleName("appInfoVersion");
		verticalPanel.add(appStatusInfo);
		appStatusInfo.setSize("500px", "14px");
		verticalPanel.setCellVerticalAlignment(appStatusInfo, HasVerticalAlignment.ALIGN_MIDDLE);

		HorizontalPanel appButtonPanel = new HorizontalPanel();
		appButtonPanel
				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		appInfoVerticalPanel.add(appButtonPanel);
		appInfoVerticalPanel.setCellWidth(appButtonPanel, "100%");
		appButtonPanel.setSize("", "");
		appInfoVerticalPanel.setCellVerticalAlignment(appButtonPanel,
				HasVerticalAlignment.ALIGN_MIDDLE);
		uploadReportButton = new PushButton("Upload Report");
		uploadReportButton.setStyleName("blueButton shadow");

		appButtonPanel.add(uploadReportButton);
		appButtonPanel.setCellVerticalAlignment(uploadReportButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appButtonPanel.setCellHorizontalAlignment(uploadReportButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		uploadReportButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				if (selected == null) {
					showMessageDialog("AppVet Error", "No app is selected",
							true);
				} else {

					reportUploadDialogBox = new ReportUploadDialogBox(userInfo,
							sessionId, selected.appId, SERVLET_URL,
							selected.os, tools);
					reportUploadDialogBox.setText("Upload Report for "
							+ selected.appName);
					reportUploadDialogBox.center();
					reportUploadDialogBox.toolNamesComboBox.setFocus(true);

					reportUploadDialogBox.cancelButton
							.addClickHandler(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									killDialogBox(reportUploadDialogBox);
								}
							});
					reportUploadDialogBox.uploadReportForm
							.addFormHandler(new ReportUploadFormHandler(
									reportUploadDialogBox, userName, selected));

				}
			}
		});
		uploadReportButton.setSize("100px", "18px");
		logButton = new PushButton("View Log");
		logButton.setStyleName("blueButton shadow");

		appButtonPanel.add(logButton);
		appButtonPanel.setCellVerticalAlignment(logButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appButtonPanel.setCellHorizontalAlignment(logButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		logButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selectedApp = appSelectionModel
						.getSelectedObject();
				showAppLog(selectedApp.appId);
			}
		});
		logButton.setSize("100px", "18px");
		deleteButton = new PushButton("Delete App");
		// deleteButton.setStyleName("appvetButton  shadow");
		deleteButton.setStyleName("blueButton shadow");

		appButtonPanel.add(deleteButton);
		appButtonPanel.setCellVerticalAlignment(deleteButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appButtonPanel.setCellHorizontalAlignment(deleteButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		deleteButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();

				deleteConfirmDialogBox = new YesNoConfirmDialog(
						"<p align=\"center\">\r\nAre you sure you want to delete app #"
								+ selected.appId + "?\r\n</p>");
				deleteConfirmDialogBox.setText("Confirm Delete");
				deleteConfirmDialogBox.center();
				deleteConfirmDialogBox.cancelButton
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								killDialogBox(deleteConfirmDialogBox);
								return;
							}
						});
				deleteConfirmDialogBox.okButton
						.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								killDialogBox(deleteConfirmDialogBox);
								if (selected != null) {
									deleteApp(selected.os, selected.appId,
											userName);
								}
							}
						});
			}
		});
		deleteButton.setSize("100px", "18px");
		downloadReportsButton = new PushButton("Download Reports");
		downloadReportsButton.setStyleName("blueButton shadow");

		appButtonPanel.add(downloadReportsButton);
		appButtonPanel.setCellVerticalAlignment(downloadReportsButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appButtonPanel.setCellHorizontalAlignment(downloadReportsButton,
				HasHorizontalAlignment.ALIGN_CENTER);

		downloadReportsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				if (selected == null) {
					showMessageDialog("AppVet Error", "No app is selected",
							true);
				} else {
					final String appId = selected.appId;
					final String dateString = "?nocache" + new Date().getTime();
					final String url = SERVLET_URL + dateString + "&"
							+ AppVetParameter.COMMAND.value + "="
							+ AppVetServletCommand.DOWNLOAD_REPORTS.name()
							+ "&" + AppVetParameter.APPID.value + "=" + appId
							+ "&" + AppVetParameter.SESSIONID.value + "="
							+ sessionId;
					Window.open(url, "_self", "");
				}
			}
		});
		downloadReportsButton.setSize("130px", "18px");

		// downloadAppButton.setStyleName("appvetButton shadow");
		downloadAppButton.setStyleName("blueButton shadow");

		appButtonPanel.add(downloadAppButton);
		appButtonPanel.setCellVerticalAlignment(downloadAppButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appButtonPanel.setCellHorizontalAlignment(downloadAppButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		downloadAppButton.setSize("100px", "18px");
		downloadAppButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final AppInfoGwt selected = appSelectionModel
						.getSelectedObject();
				if (selected == null) {
					showMessageDialog("AppVet Error", "No app is selected",
							true);
				} else {
					final String appId = selected.appId;
					final String dateString = "?nocache" + new Date().getTime();
					final String url = SERVLET_URL + dateString + "&"
							+ AppVetParameter.COMMAND.value + "="
							+ AppVetServletCommand.DOWNLOAD_APP.name() + "&"
							+ AppVetParameter.APPID.value + "=" + appId + "&"
							+ AppVetParameter.SESSIONID.value + "=" + sessionId;
					Window.open(url, "_self", "");
				}
			}
		});
		logButton.setVisible(true);

		uploadReportButton.setVisible(true);

		horizontalPanel.setCellVerticalAlignment(uploadReportButton,
				HasVerticalAlignment.ALIGN_MIDDLE);

		horizontalPanel.setCellVerticalAlignment(logButton,
				HasVerticalAlignment.ALIGN_MIDDLE);

		horizontalPanel.setCellVerticalAlignment(deleteButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel.setCellHorizontalAlignment(downloadReportsButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.setCellVerticalAlignment(downloadReportsButton,
				HasVerticalAlignment.ALIGN_MIDDLE);
		appsListButtonPanel.setCellHorizontalAlignment(downloadReportsButton,
				HasHorizontalAlignment.ALIGN_CENTER);
		toolResultsHtml = new HTML("", true);
		appInfoVerticalPanel.add(toolResultsHtml);
		toolResultsHtml.setWidth("561px");
		toolResultsHtml
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		toolResultsHtml.setStyleName("toolResultsHtml");
		appInfoVerticalPanel.setCellWidth(toolResultsHtml, "100%");

		SimplePanel southPanel = new SimplePanel();
		southPanel.setStyleName("southPanel");
		addSouth(southPanel, 40.0);
		southPanel.setSize("", "");

		VerticalPanel verticalPanel_1 = new VerticalPanel();
		verticalPanel_1.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel_1
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		southPanel.setWidget(verticalPanel_1);
		verticalPanel_1.setSize("100%", "");

		HorizontalPanel horizontalPanel_2 = new HorizontalPanel();
		verticalPanel_1.add(horizontalPanel_2);
		horizontalPanel_2.setSize("100%", "");
		verticalPanel_1.setCellVerticalAlignment(horizontalPanel_2,
				HasVerticalAlignment.ALIGN_MIDDLE);
		verticalPanel_1.setCellWidth(horizontalPanel_2, "100%");
		horizontalPanel_2
				.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_2
				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

		Image orgLogoMain = new Image("../appvet_images/org_logo_main.png");
		orgLogoMain.setAltText("Org Logo");
		horizontalPanel_2.add(orgLogoMain);
		horizontalPanel_2.setCellWidth(orgLogoMain, "50%");
		orgLogoMain.setSize("126px", "37px");
		horizontalPanel_2.setCellVerticalAlignment(orgLogoMain,
				HasVerticalAlignment.ALIGN_MIDDLE);
		horizontalPanel_2.setCellHorizontalAlignment(orgLogoMain,
				HasHorizontalAlignment.ALIGN_LEFT);

		Image nistLogo = new Image("images/nist_logo_darkgrey.png");

		nistLogo.setAltText("NIST logo");

		// nistLogo.setTitle("NIST logo");
		horizontalPanel_2.add(nistLogo);
		horizontalPanel_2.setCellWidth(nistLogo, "50%");

		horizontalPanel_2.setCellVerticalAlignment(nistLogo,
				HasVerticalAlignment.ALIGN_MIDDLE);

		horizontalPanel_2.setCellHorizontalAlignment(nistLogo,
				HasHorizontalAlignment.ALIGN_RIGHT);
		nistLogo.setSize("65px", "17px");

		if ((initialApps != null) && (initialApps.apps.size() > 0)) {
			appSelectionModel.setSelected(initialApps.apps.get(0), true);
		} else {
			disableAllButtons();
		}

		scheduleResize();

		// Note that SSO users can manually refresh the page but non-SSO users
		// will
		// return to AppVet login page upon a manual refresh of the page.
		if (!ssoActive) {
			showDontRefreshWarning();
		}
	}

	public void showAppLog(final String appId) {
		appVetServiceAsync.getAppLog(appId,
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"Could not access app log", true);
					}

					@Override
					public void onSuccess(String result) {
						final LogViewer logViewer = new LogViewer(result);
						logViewer.setText("App " + appId + " log");
						logViewer.center();
						logViewer.closeButton.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								killDialogBox(logViewer);
							}
						});
					}

				});
	}
	
	public void showAppVetLog() {
		appVetServiceAsync.getAppVetLog(new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"Could not access AppVet log", true);
					}

					@Override
					public void onSuccess(String result) {
						final LogViewer logViewer = new LogViewer(result);
						logViewer.setText("AppVet log");
						logViewer.center();
						logViewer.closeButton.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								killDialogBox(logViewer);
							}
						});
					}

				});
	}

	public void enableAllButtons() {
		logButton.setEnabled(true);
		uploadReportButton.setEnabled(true);
		if (keepApps)
			deleteButton.setEnabled(true);
		downloadReportsButton.setEnabled(true);
		downloadReportsButton.setEnabled(true);
		downloadAppButton.setEnabled(true);
	}

	public void disableAllButtons() {
		logButton.setEnabled(false);
		uploadReportButton.setEnabled(false);
		deleteButton.setEnabled(false);
		downloadReportsButton.setEnabled(false);
		downloadReportsButton.setEnabled(false);
		downloadAppButton.setEnabled(false);
	}

	public void removeSession(final boolean sessionExpired) {
		// First stop polling the server for data
		pollingTimer.cancel();
		killDialogBox(messageDialogBox);

		appVetServiceAsync.removeSession(sessionId,
				new AsyncCallback<Boolean>() {

					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"App list retrieval error", true);
					}

					@Override
					public void onSuccess(Boolean result) {
						if (result == false) {
							showMessageDialog("AppVet Error",
									"Could not remove session", true);
						} else {
							if (sessionExpired) {
								// Show session expired message
								showMessageDialog("AppVet Session",
										"AppVet session has expired", true);
								messageDialogBox.closeButton
										.addClickHandler(new ClickHandler() {
											@Override
											public void onClick(ClickEvent event) {
												killDialogBox(messageDialogBox);
											}
										});
							}
							if (ssoActive) {
								logoutSSO();
							} else {
								logoutNonSSO();
							}

						}
					}

				});
	}

	public void setAlertMessage(String username, SystemAlertType alertType,
			String alertMessage) {
		SystemAlert alert = new SystemAlert();
		alert.type = alertType;
		alert.message = alertMessage;

		appVetServiceAsync.setAlertMessage(username, alert,
				new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"Could not set alert message", true);
					}

					@Override
					public void onSuccess(Boolean setAlert) {
						if (!setAlert) {
							showMessageDialog("AppVet Error",
									"Could not set alert message", true);
						} else {
							// log.info("Alert message set");
						}
					}
				});
	}

	public void clearAlertMessage(String username) {
		appVetServiceAsync.clearAlertMessage(username,
				new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"Could not clear alert message", true);
					}

					@Override
					public void onSuccess(Boolean setAlert) {
						if (!setAlert) {
							showMessageDialog("AppVet Error",
									"Could not clear alert message", true);
						} else {
							// log.info("Alert message cleared");
						}
					}
				});
	}

	public void clearLog() {
		appVetServiceAsync.clearLog(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				showMessageDialog("AppVet Error",
						"AppVet log could not be cleared", true);
			}

			@Override
			public void onSuccess(Boolean logCleared) {
				if (logCleared == false) {
					showMessageDialog("AppVet Error",
							"AppVet log could not be cleared", true);
				} else {
					showMessageDialog("AppVet", "AppVet log cleared", false);
				}
			}
		});
	}

	public void deleteApp(final DeviceOS os, final String appid,
			final String username) {
		appVetServiceAsync.deleteApp(os, appid, username,
				new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						showMessageDialog("AppVet Error",
								"App list retrieval error", true);
					}

					@Override
					public void onSuccess(Boolean deleted) {
						if (deleted == false) {
							showMessageDialog("AppVet Error",
									"Could not delete app", true);
						} else {
							final AppInfoGwt currentlySelectedApp = appSelectionModel
									.getSelectedObject();
							final int currentlySelectedIndex = getAppsListIndex(
									currentlySelectedApp, allApps);
							for (int i = 0; i < allApps.size(); i++) {
								final AppInfoGwt appInfoGwt = allApps.get(i);
								if (appInfoGwt.appId.equals(appid)) {
									allApps.remove(i);
									if (!searchMode) {
										appsListTable.remove(i);
									} else {
										appsListTable.remove(appid);
									}
									break;
								}
							}
							if (!searchMode) {
								if (allApps.size() > 0) {
									appSelectionModel.setSelected(
											allApps.get(currentlySelectedIndex),
											true);
								} else {
									appInfoVersion.setHTML("");
									appInfoPackage.setHTML("");
									appStatusInfo.setHTML("");
									appInfoIcon.setVisible(false);
									appInfoName.setText("");
									toolResultsHtml.setText("");
									disableAllButtons();

								}
							}
						}
					}
				});
	}

	public int getAppsListIndex(AppInfoGwt item, List<AppInfoGwt> appsList) {
		if (item != null) {
			for (int i = 0; i < appsList.size(); i++) {
				if (item.appId.equals(appsList.get(i).appId)) {
					return i;
				}
			}
		}
		return 0;
	}

	public void getServerUpdates(String username) {
		// Get (1) session expiration, (2) updated apps, and (3) alert updates
		appVetServiceAsync.getServerUpdates(username, sessionId,
				sessionExpiration, lastAppsListUpdate,
				new AsyncCallback<ServerPacket>() {

					@Override
					public void onFailure(Throwable caught) {
						log.severe("Could not update from server: "
								+ caught.getMessage());
					}

					@Override
					public void onSuccess(ServerPacket serverPacket) {
						if (serverPacket == null) {
							log.severe("Error updating server packet.");
							removeSession(true);
						} else {
							// Get session expiration
							Date newSessionExpiration = serverPacket
									.getSessionExpiration();
							if (newSessionExpiration == null) {
								// log.severe("Error updating session expiration. Session probably expired.");
								removeSession(true);
							} else {
								sessionTimeLeft(newSessionExpiration);
							}

							// Get alert message
							SystemAlert systemAlert = serverPacket
									.getSystemAlert();
							if (systemAlert != null) {
								// log.info("system alert is not null. Setting message: "
								// +
								// systemAlert.message);

								String newSpanValue = systemAlert.message;
								// log.info("newSpanValue: " + newSpanValue);
								String newAlertMessage = null;
								String newAltValue = null;

								String currentAlertMessage = statusMessageHtml
										.getHTML();
								int startSpanIndex = currentAlertMessage
										.indexOf("<span style=\"\">");
								int endSpanIndex = currentAlertMessage.indexOf(
										"</span>", startSpanIndex + 15);
								String currentSpanValue = currentAlertMessage
										.substring(startSpanIndex + 15,
												endSpanIndex);
								// log.info("currentSpanValue: " +
								// currentSpanValue);

								int startAltIndex = currentAlertMessage
										.indexOf("alt=\"");
								int endAltIndex = currentAlertMessage.indexOf(
										"\"", startAltIndex + 5);
								String currentAltValue = currentAlertMessage
										.substring(startAltIndex + 5,
												endAltIndex);
								// log.info("currentAltValue: " +
								// currentAltValue);

								// log.info("--------- checking messages ---------------");
								if (systemAlert.type == SystemAlertType.NORMAL) {
									newAlertMessage = "<div><img style=\"vertical-align:bottom\" width=\"18px\" height=\"18px\" src=\"images/icon-metadata.png\" alt=\"System Message\" /> <span style=\"\">"
											+ newSpanValue + "</span></div>";
									newAltValue = "System Message";
									// log.info("newAltValue: " + newAltValue);

								} else if (systemAlert.type == SystemAlertType.WARNING) {
									newAlertMessage = "<div><img style=\"vertical-align:bottom\" width=\"18px\" height=\"18px\" src=\"images/icon-warning.png\" alt=\"Warning Message\" /> <span style=\"\">"
											+ newSpanValue + "</span></div>";
									newAltValue = "Warning Message";
									// log.info("newAltValue: " + newAltValue);

								} else if (systemAlert.type == SystemAlertType.CRITICAL) {
									newAlertMessage = "<div><img style=\"vertical-align:bottom\" width=\"18px\" height=\"18px\" src=\"images/icon-error.png\" alt=\"Error Message\" /> <span style=\"\">"
											+ newSpanValue + "</span></div>";
									newAltValue = "Error Message";
									// log.info("newAltValue: " + newAltValue);
								}

								// Only set if new message is different from
								// current message
								if (currentSpanValue.equals(newSpanValue)
										&& currentAltValue.equals(newAltValue)) {
									// log.info("New message is same as current message");
								} else {
									statusMessageHtml.setHTML(newAlertMessage);
								}
							} else {
								// log.info("system alert is null. Setting message to ''");
								statusMessageHtml.setHTML("");
							}

							// Get updated apps
							AppsListGwt updatedAppsList = serverPacket
									.getUpdatedAppsList();
							if (updatedAppsList == null) {
								showMessageDialog("AppVet Database Error",
										"Could not retrieve updated apps", true);
							} else {
								// log.info("Update time: " +
								// updatedAppsList.appsLastChecked.toString());
								lastAppsListUpdate = updatedAppsList.appsLastChecked;
								if (updatedAppsList.apps.size() > 0) {
									setUpdatedApps(updatedAppsList.apps);
								}
							}

						}
					}
				});

	}

	@Override
	public void onBrowserEvent(Event event) {
		sessionExpiration = new Date(System.currentTimeMillis()
				+ MAX_SESSION_IDLE_DURATION);
	}

	public synchronized void displaySelectedAppInfo(final AppInfoGwt selectedApp) {
		// Show selected app info results
		if (selectedApp != null) {
			appVetServiceAsync.getToolsResults(selectedApp.os, sessionId,
					selectedApp.appId,
					new AsyncCallback<List<ToolStatusGwt>>() {

						@Override
						public void onFailure(Throwable caught) {
							showMessageDialog("AppVet Error",
									"System error retrieving app info", true);
						}

						@Override
						public void onSuccess(List<ToolStatusGwt> toolsResults) {

							if ((toolsResults == null)
									|| toolsResults.isEmpty()) {
								showMessageDialog("AppVet Error: ",
										"Could not retrieve app info.", true);
							} else {
								// Display selected app information
								String iconPath = null;
								String altText = null;
								if (selectedApp.iconURL == null) {
									// Icon has not yet been generated for this
									// app
									if (selectedApp.os == DeviceOS.ANDROID) {
										iconPath = "images/android-icon-gray.png";
										altText = "Android app";
									} else if (selectedApp.os == DeviceOS.IOS) {
										iconPath = "images/apple-icon-gray.png";
										altText = "iOS app";
									}
								} else {
									// Icon has been generated for this app
									iconPath = selectedApp.iconURL;
									altText = selectedApp.appName;
								}

								appInfoIcon.setVisible(true);
								appInfoIcon.setUrl(iconPath);
								appInfoIcon.setAltText(altText);

								// Set app name in right info panel
								String appNameHtml = null;
								if ((selectedApp.appStatus == AppStatus.NA)
										|| (selectedApp.appStatus == AppStatus.ERROR)
										|| (selectedApp.appStatus == AppStatus.HIGH)
										|| (selectedApp.appStatus == AppStatus.HIGH_WITH_ERROR)
										|| (selectedApp.appStatus == AppStatus.MODERATE)
										|| (selectedApp.appStatus == AppStatus.MODERATE_WITH_ERROR)
										|| (selectedApp.appStatus == AppStatus.LOW)
										|| (selectedApp.appStatus == AppStatus.LOW_WITH_ERROR)
										) {
									appNameHtml = "<div id=\"appNameInfo\">"
											+ selectedApp.appName + "</div>";
									enableAllButtons();

								} else {
									appNameHtml = "<div id=\"appNameInfo\">"
											+ selectedApp.appName + "</div>";
									uploadReportButton.setEnabled(false);
									logButton.setEnabled(true);
									deleteButton.setEnabled(false);
									downloadReportsButton.setEnabled(false);
									downloadReportsButton.setEnabled(false);
									downloadAppButton.setEnabled(false);
								}

								// Set app package in right info panel
								appInfoName.setHTML(appNameHtml);
								if ((selectedApp.packageName == null)
										|| selectedApp.packageName.equals("")) {
									appInfoPackage
											.setHTML("<b>Package: </b>N/A");
								} else {
									appInfoPackage.setHTML("<b>Package: </b>"
											+ selectedApp.packageName);
								}

								// Set version in right info panel
								if ((selectedApp.versionName == null)
										|| selectedApp.versionName.equals("")) {
									appInfoVersion
											.setHTML("<b>Version: </b>N/A");
								} else {
									appInfoVersion.setHTML("<b>Version: </b>"
											+ selectedApp.versionName);
								}
								
								// Set status in right info panel
								String status = null;
								AppStatus appStatus = selectedApp.appStatus;
								if (appStatus == null) {
									status = "<div style=\"display: inline\" id=\"naStatus\">"
											+ "<b>N/A</b>" + "</div>";
								} else if (appStatus == AppStatus.HIGH) {
									status = "<div style=\"display: inline\" id=\"highStatus\">"
											+ "<b>HIGH</b>" + "</div>";
								} else if (appStatus == AppStatus.HIGH_WITH_ERROR) {
									status = "<div style=\"display: inline\" id=\"highStatus\">"
											+ "<b>HIGH*</b>" + "</div>";
								} else if (appStatus == AppStatus.MODERATE) {
									status = "<div style=\"display: inline\" id=\"moderateStatus\">"
											+ "<b>MODERATE</b>" + "</div>";									
								} else if (appStatus == AppStatus.MODERATE_WITH_ERROR) {
									status = "<div style=\"display: inline\" id=\"moderateStatus\">"
											+ "<b>MODERATE*</b>" + "</div>";	
								} else if (appStatus == AppStatus.LOW) {
									status = "<div style=\"display: inline\" id=\"lowStatus\">"
											+ "<b>LOW</b>" + "</div>";	
								} else if (appStatus == AppStatus.LOW_WITH_ERROR) {
									status = "<div style=\"display: inline\" id=\"lowStatus\">"
											+ "<b>LOW*</b>" + "</div>";	
								} else if (appStatus == AppStatus.NA) {
									status = "<div style=\"display: inline\" id=\"naStatus\">"
											+ "<b>N/A</b>" + "</div>";
								} else {
									status = "<div style=\"display: inline\" id=\"normalStatus\">"
											+ "<b>" + appStatus.name() + "</b></div>";
								}
									
								// Set status display
								appStatusInfo.setHTML("<b>Status: </b>"
										+ status);

								// Get tool results
								final String htmlToolResults = getHtmlToolResults(
										selectedApp.appId, toolsResults);
								toolResultsHtml.setHTML(htmlToolResults);
								logButton.setEnabled(true);
							}
						}

						// Display all reports
						public String getHtmlToolResults(String appId,
								List<ToolStatusGwt> toolResults) {
							// Get summary report
							String statuses = "<h3 title=\"Overview\" id=\"appInfoSectionHeader\">Overview</h3>\n";
							int summaryCount = 0;

							for (int i = 0; i < toolResults.size(); i++) {
								ToolType analysisType = toolResults.get(i)
										.getToolType();

								if (analysisType == ToolType.SUMMARY) { // TODO:
																		// For
																		// AV3,
																		// SUMMARY
																		// was
																		// removed
																		// (now
																		// uses
																		// only
																		// REPORT)
									summaryCount++;
									statuses += getToolStatusHtmlDisplay(toolResults
											.get(i));
								}
							}

							if (summaryCount == 0) {
								statuses += getNAStatus();
							}

							// Get pre-processing analysis results
							statuses += "<h3 title=\"App Information\" id=\"appInfoSectionHeader\">App Information</h3>\n";
							int preprocessorToolCount = 0;

							for (int i = 0; i < toolResults.size(); i++) {
								ToolType toolType = toolResults.get(i)
										.getToolType();

								if (toolType == ToolType.PREPROCESSOR) {
									preprocessorToolCount++;
									statuses += getPreprocessorStatusHtmlDisplay(toolResults
											.get(i));
								}
							}

							if (preprocessorToolCount == 0) {
								statuses += getNAStatus();
							}

							// Get tool and manually-uploaded results.
							statuses += "<h3 title=\"Report Type\"  id=\"appInfoSectionHeader\">Report Type</h3>\n";
							int analysisToolCount = 0;

							for (int i = 0; i < toolResults.size(); i++) {
								ToolType toolType = toolResults.get(i)
										.getToolType();

								if (toolType == ToolType.TESTTOOL
										|| toolType == ToolType.REPORT) {
									analysisToolCount++;
									statuses += getToolStatusHtmlDisplay(toolResults
											.get(i));
								}
							}

							if (analysisToolCount == 0) {
								statuses += getNAStatus();
							}

							/* Get audit results */
//							statuses += "<h3 title=\"Final Organizational Determination\" id=\"appInfoSectionHeader\">Final Organizational Determination</h3>\n";
//							int auditCount = 0;
//
//							for (int i = 0; i < toolResults.size(); i++) {
//								ToolType toolType = toolResults.get(i)
//										.getToolType();
//
//								if (toolType == ToolType.AUDIT) { // TODO: For
//																	// AV3,
//																	// AUDIT was
//																	// removed
//																	// (now uses
//																	// only
//																	// REPORT)
//									auditCount++;
//									statuses += getToolStatusHtmlDisplay(toolResults
//											.get(i));
//								}
//							}
//
//							if (auditCount == 0) {
//								statuses += getNAStatus();
//							}

							return statuses;
						}

						public String getNAStatus() {
							return "<table>\n"
									+ "<tr>\n"
									+ "<td title=\"NA status\" align=\"left\" style='color: dimgray; size:18; weight: bold'width=\"185\">"
									+ "N/A" + "</td>\n" + "</tr>\n"
									+ "</table>\n";
						}

						public String getPreprocessorStatusHtmlDisplay(
								ToolStatusGwt toolStatus) {
							String status = null;

							if (toolStatus.getStatusHtml().indexOf("LOW") > -1) {
								// Pre-processor status of LOW is displayed as
								// "COMPLETED"
								status = "<div id=\"tabledim\" style='color: black'>COMPLETED</div>";
							} else {
								status = toolStatus.getStatusHtml();
							}

							String toolIconURL = null;
							String toolIconAltText = null;
							if (toolStatus.getIconURL() != null) {
								// Check for custom icon URL defined in tool
								// adapter config file
								toolIconURL = toolStatus.getIconURL();
								toolIconAltText = toolStatus.getIconAltText();
							} else {
								// Use default icons
								toolIconURL = toolStatus.getToolType()
										.getDefaultIconURL();
								toolIconAltText = toolStatus.getToolType()
										.getDefaultAltText();
							}

							// To over on table, add 'class=\"hovertable\"
							return getToolRowHtml(toolIconURL, toolIconAltText,
									toolStatus.getToolDisplayName(), status,
									toolStatus.getReport());
						}

						public String getToolStatusHtmlDisplay(
								ToolStatusGwt toolStatus) {
							String toolIconURL = null;
							String toolIconAltText = null;

							if (toolStatus.getIconURL() != null) {
								toolIconURL = toolStatus.getIconURL();
								toolIconAltText = toolStatus.getIconAltText();
							} else {
								toolIconURL = toolStatus.getToolType()
										.getDefaultIconURL();
								toolIconAltText = toolStatus.getToolType()
										.getDefaultAltText();
							}

							return getToolRowHtml(toolIconURL, toolIconAltText,
									toolStatus.getToolDisplayName(),
									toolStatus.getStatusHtml(),
									toolStatus.getReport());
						}

						private String getToolRowHtml(String toolIconURL,
								String toolIconAltText, String toolDisplayName,
								String toolStatus, String toolReport) {
							return "<table>" + "<tr>\n"
									+ "<td>"
									+ "<img class=\"toolimages\" src=\""
									+ toolIconURL
									+ "\" alt=\""
									+ toolIconAltText
									+ "\"> "
									+ "</td>\n"
									// Removed title="mytitle" from following td
									+ "<td align=\"left\" width=\"200\">"
									+ toolDisplayName
									+ "</td>\n"
									// Removed title="mytitle" from following td
									+ "<td align=\"left\" width=\"140\">"
									+ toolStatus
									+ "</td>\n"
									// Removed title="mytitle" from following td
									+ "<td align=\"left\" width=\"45\">"
									+ toolReport + "</td>\n" + "</tr>\n"
									+ "</table>";
						}

					});
		}
	}

	public void pollServer(final String username) {
		// final String user = username;
		pollingTimer = new Timer() {
			@Override
			public void run() {
				// The following methods hit the database. To increase
				// performance,
				// it might be good to combine the functionality of these three
				// methods into a single method call to the server (and
				// database).
				getServerUpdates(username);
			}
		};
		pollingTimer.scheduleRepeating(POLLING_INTERVAL);
	}

	public void showDontRefreshWarning() {
		warningTimer = new Timer() {
			@Override
			public void run() {
				if (allApps.size() <= NUM_APPS_SHOW_REFRESH_WARNING) {
					showMessageDialog(
							"AppVet Info",
							"AppVet is a dynamic web application that automatically updates "
									+ " in real-time. Do not refresh or reload this page while using AppVet.",
							true);
				}
			}
		};
		warningTimer.schedule(1000);
	}

	/**
	 * This method resizes the center panel and appsListTable.
	 */
	public void adjustComponentSizes() {
		/**
		 * The following variable is the main variable to adjust when changing
		 * the size of the org_logo_main.png image. The larger this image, the
		 * larger MARGIN_HEIGHT should be. Note these parameters will be
		 * rendered differently on Firefox, Chrome and IE, with Firefox being
		 * the most problematic, so check all three browsers!
		 */
		final int appVetPanelHeight = this.getOffsetHeight(); // Total height
																// including
																// decoration
																// and padding,
																// but not
																// margin

		// Set center panel height
		final int MARGIN_HEIGHT = 0;
		final int centerPanelHeight = appVetPanelHeight
				- (int) NORTH_PANEL_HEIGHT - (int) SOUTH_PANEL_HEIGHT
				- MARGIN_HEIGHT;
		int adjustedCenterPanelHeight = centerPanelHeight + 12; // Adjust center
																// panel height
		centerPanel.setHeight(adjustedCenterPanelHeight + "px");

		// Set pager height to adjust appsListTable height inside center panel
		int PAGER_HEIGHT = 58;
		final int appsListTableHeight = centerPanelHeight - PAGER_HEIGHT;
		appsListTable.setHeight(appsListTableHeight + "px");
		appsListTable.dataGrid.redraw();

	}

	// The size of the AppVet panel is 0 until displayed in rootlayoutpanel.
	public void scheduleResize() {
		final Timer resizeTimer = new Timer() {
			@Override
			public void run() {
				adjustComponentSizes();
			}
		};
		resizeTimer.schedule(250);
	}

	public int search() {
		searchMode = true;
		final String[] tokens = searchTextBox.getValue().split("\\s+");
		if (tokens == null) {
			return 0;
		}
		final ArrayList<AppInfoGwt> searchList = new ArrayList<AppInfoGwt>();
		for (int i = 0; i < tokens.length; i++) {
			if (Validate.isLegalSearchString(tokens[i])) {
				for (int j = 0; j < allApps.size(); j++) {
					final AppInfoGwt appInfoSummary = allApps.get(j);
					if (appInfoSummary.tokenMatch(tokens[i])) {
						searchList.add(appInfoSummary);
					}
				}
			} else {
				log.warning("Search token: " + tokens[i] + " is not valid");
			}
		}
		searchTextBox.setText("Search");
		if (searchList.size() == 0) {
			showMessageDialog("Search Results", "No search results were found",
					true);
			return 0;
		} else {
			appsListTable.setDataList(searchList);
			appSelectionModel.setSelected(searchList.get(0), true);
			// Set View All to visible
			viewAllButton.setVisible(true);
			return searchList.size();
		}
	}

	public void setAllApps() {
		final SafeHtmlBuilder sb = new SafeHtmlBuilder();
		sb.appendHtmlConstant("<h3>Apps</h3>");
		appsLabelHtml.setHTML(sb.toSafeHtml());
		appsListTable.setDataList(allApps);
	}

	public void setUpdatedApps(List<AppInfoGwt> updatedAppsList) {
		for (int i = 0; i < updatedAppsList.size(); i++) {
			final AppInfoGwt updatedAppInfo = updatedAppsList.get(i);
			int matchIndex = -1;
			for (int j = 0; j < allApps.size(); j++) {
				final AppInfoGwt appInList = allApps.get(j);
				if (updatedAppInfo.appId.equals(appInList.appId)) {
					matchIndex = j;
					break;
				}
			}
			if (matchIndex > -1) {
				// overwrites existing app
				allApps.set(matchIndex, updatedAppInfo);
				if (!searchMode) {
					appsListTable.set(matchIndex, updatedAppInfo);
				}
				// Check if updated app status indicates an error with a tool
				if (updatedAppInfo.appStatus == AppStatus.LOW_WITH_ERROR
						|| updatedAppInfo.appStatus == AppStatus.MODERATE_WITH_ERROR
						|| updatedAppInfo.appStatus == AppStatus.HIGH_WITH_ERROR) {
					// A tool must have encountered an error, so alert the user
					showMessageDialog(
							"Tool Error",
							"An error with one or more tools was detected for app "
									+ updatedAppInfo.appId
									+ ". The reports for these tool may or may not arrive at a later time. "
									+ "These tools has been disabled and the administrator has been notified.",
							true);

				}
			} else {
				// adds new app
				allApps.add(0, updatedAppInfo);
				if (!searchMode) {
					appsListTable.add(0, updatedAppInfo);
				}
			}
		}

		final AppInfoGwt currentlySelectedApp = appSelectionModel
				.getSelectedObject();

		if (currentlySelectedApp == null) {
			return;
		}

		final int currentlySelectedIndex = getAppsListIndex(
				currentlySelectedApp, allApps);

		if (currentlySelectedIndex < 0) {
			return;
		}

		if (!searchMode) {
			if (allApps.size() > 0) {
				appSelectionModel.setSelected(
						allApps.get(currentlySelectedIndex), true);
			} else {
				appInfoIcon.setVisible(false);
				appInfoName.setText("");
				toolResultsHtml.setText("");
				disableAllButtons();
			}
		}
	}

	public void sessionTimeLeft(Date expirationTime) {
		Date currentDate = new Date();
		long diff = expirationTime.getTime() - currentDate.getTime();
		// log.info("diff: " + diff);
		if (diff <= 0) {
			// Session timed-out
			removeSession(true);
		} else if (diff <= 60000 && timeoutWarningMessage == false) {
			// 60 seconds left before timeout, alert user
			// Close current message if its exists
			killDialogBox(messageDialogBox);
			// Now show timeout dialog
			showTimeoutDialog(diff);
		} else if (diff <= 6000 && timeoutWarningMessage == true) {
			// Timeout warning already displayed. Do nothing.
		} else {
			// Do nothing
		}
	}

	public void openToolCredentials(final ConfigInfoGwt configInfoGwt) {
		final ToolAuthParamDialogBox toolAuthParamDialogBox = new ToolAuthParamDialogBox(
				configInfoGwt);
		toolAuthParamDialogBox.setText("Tool Account Information");
		toolAuthParamDialogBox.center();

		toolAuthParamDialogBox.okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				toolAuthParamDialogBox.hide();
			}
		});
	}

	/**
	 * If configuration information for a user is changed by an ADMIN during the
	 * time the user is logged in then the change is not visible to the user
	 * until the user's next log in.
	 */
	public void openUserAccount(final ConfigInfoGwt configInfo) {

		if (configInfo.getUserInfo().isDefaultAdmin()) {
			showMessageDialog("Account Info", "Cannot change info for "
					+ "default AppVet administrator", false);
			return;
		}

		userAcctDialogBox = new UserAcctDialogBox(configInfo, ssoActive);
		userAcctDialogBox.setText("Account Settings");
		userAcctDialogBox.center();
		userAcctDialogBox.password1TextBox.setFocus(true);
		userAcctDialogBox.cancelButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				killDialogBox(userAcctDialogBox);
			}
		});
		userAcctDialogBox.okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				// killDialogBox(userAcctDialogBox);
				final String newLastName = userAcctDialogBox.lastNameTextBox
						.getText();
				final String newFirstName = userAcctDialogBox.firstNameTextBox
						.getText();
				final String newEmail = userAcctDialogBox.emailTextBox
						.getText();
				final String newPassword1 = userAcctDialogBox.password1TextBox
						.getValue();
				final String newPassword2 = userAcctDialogBox.password2TextBox
						.getValue();
				final UserInfo updatedUserInfo = new UserInfo();
				updatedUserInfo.setUserName(userInfo.getUserName());
				updatedUserInfo.setLastName(newLastName);
				updatedUserInfo.setFirstName(newFirstName);
				updatedUserInfo.setEmail(newEmail);
				updatedUserInfo.setPasswords(newPassword1, newPassword2);
				updatedUserInfo.setRoleAndOrgMembership(userInfo
						.getRoleAndOrgMembership());
				// Validate updated user info
				if (!userInfoIsValid(updatedUserInfo, ssoActive)) {
					return;
				}

				appVetServiceAsync.selfUpdatePassword(updatedUserInfo,
						new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								showMessageDialog("Update Error",
										"Could not update user information",
										true);
								killDialogBox(userAcctDialogBox);
							}

							@Override
							public void onSuccess(Boolean result) {
								final boolean updated = result.booleanValue();
								if (updated) {
									userMenuItem.setText(userInfo
											.getNameWithLastNameInitial());
									userInfo.setUserName(userInfo.getUserName());
									userInfo.setLastName(updatedUserInfo
											.getLastName());
									userInfo.setFirstName(updatedUserInfo
											.getFirstName());
									userInfo.setEmail(updatedUserInfo
											.getEmail());
									updatedUserInfo.setPassword("");

									killDialogBox(userAcctDialogBox);
									showMessageDialog("Account Update",
											"Password updated successfully.",
											false);
								} else {
									showMessageDialog(
											"Update Error",
											"Could not update user information",
											true);
									killDialogBox(userAcctDialogBox);
								}
							}
						});

			}
		});

	}
}
