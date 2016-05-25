package com.blackducksoftware.tools.testhubclient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.hub.HubNotificationDao;
import com.blackducksoftware.tools.testhubclient.model.component.ComponentVersion;
import com.blackducksoftware.tools.testhubclient.model.component.VulnerableComponentItem;
import com.blackducksoftware.tools.testhubclient.model.notification.ComponentVersionStatus;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerableComponentsResponse;
import com.blackducksoftware.tools.testhubclient.model.policy.ApprovalStatusItem;
import com.blackducksoftware.tools.testhubclient.model.policy.BomComponentVersionPolicyStatus;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyRule;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyStatus;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersion;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersionItem;
import com.blackducksoftware.tools.testhubclient.model.vulnerability.RemediationStatus;
import com.blackducksoftware.tools.testhubclient.service.NotificationService;
import com.blackducksoftware.tools.testhubclient.service.NotificationServiceException;
import com.blackducksoftware.tools.testhubclient.service.impl.NotificationServiceImpl;

public class HubCommonClient {

	public static void main(String[] args) throws Exception {

		System.out.println("Starting up...");
		System.out.println((new Date()).toString());

		String username = "sysadmin";
		String password = "blackduck";

		NotificationDao dao = new HubNotificationDao("http://eng-hub-valid03.dc1.lan", username, password,
				"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		NotificationService svc = new NotificationServiceImpl(dao);
		HubCommonClient client = new HubCommonClient(svc);

		client.run("2016-05-01T00:00:00.000Z", "2016-05-11T00:00:00.000Z", 1000);
		System.out.println((new Date()).toString());
	}

	private final ClientLogger log;
	private final NotificationService svc;

	private Map<Integer, JiraTicket> tickets = new HashMap<>();

	private int duplicateCount = 0;
	private int ticketCount = 0;

	public HubCommonClient(NotificationService svc) throws Exception {
		this.svc = svc;
		log = new ClientLogger();
		String hubVersion = svc.getVersion();
		log.info("Hub version: " + hubVersion);
	}

	public Statistics run(String startDate, String endDate, int limit) throws Exception {

		int notificationCount = processNotifications(startDate, endDate, limit);

		log.info("Done processsing " + notificationCount + " notifications, generating " + ticketCount + " tickets, "
				+ duplicateCount + " of which were duplicates");

		return new Statistics(notificationCount, ticketCount, duplicateCount);

	}

	private int processNotifications(String startDate, String endDate, int limit) throws Exception {

		List<NotificationItem> notificationItems = svc.getNotifications(startDate, endDate, limit);
		for (NotificationItem notificationItem : notificationItems) {

			Date notificationTimeStamp = notificationItem.getCreatedAt();
			log.info("\n\n======================================================================\n"
					+ "NotificationItem: " + notificationItem);
			if (notificationItem instanceof VulnerabilityNotificationItem) {
				processVulnerabilityNotification(notificationTimeStamp,
						(VulnerabilityNotificationItem) notificationItem);
			} else if (notificationItem instanceof RuleViolationNotificationItem) {
				processRuleViolationNotification(notificationTimeStamp,
						(RuleViolationNotificationItem) notificationItem);
			} else if (notificationItem instanceof PolicyOverrideNotificationItem) {
				processPolicyOverrideNotification(notificationTimeStamp,
						(PolicyOverrideNotificationItem) notificationItem);
			} else {
				log.error("Unknown notification type: " + notificationItem.getType() + ": " + notificationItem);
			}
		}
		return notificationItems.size();
	}

	private void processPolicyOverrideNotification(Date notificationTimeStamp,
			PolicyOverrideNotificationItem policyOverrideNotif) throws Exception {
		BomComponentVersionPolicyStatus compPolicyStatus = getCompPolicyStatusFromLink(policyOverrideNotif.getContent()
				.getBomComponentVersionPolicyStatusLink());
		PolicyStatus policyStatus = null;

		if (compPolicyStatus != null) {
			policyStatus = compPolicyStatus.getOverallStatus();
		} else {
			log.error("Component Policy Status is null");
		}
		log.info("Overall policy status: " + policyStatus);
		log.info("\tWas updated at: " + compPolicyStatus.getUpdatedAt());
		if (!PolicyStatus.IN_VIOLATION_OVERRIDEN.equals(policyStatus)) {
			log.info("No ticket being generated");
			return;
		}

		JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp, JiraTicketType.POLICY_OVERRIDE,
				policyOverrideNotif.getContent().getProjectName(), policyOverrideNotif.getContent()
						.getProjectVersionName(), policyOverrideNotif.getContent().getComponentName(),
				policyOverrideNotif.getContent().getComponentVersionName(), null, null, ActionRequired.REVIEW);
		System.out.println(jiraTicket);
		addToMap(jiraTicket);
	}

	private void addToMap(JiraTicket jiraTicket) {
		ticketCount++;
		if (tickets.containsKey(jiraTicket.hashCode())) {
			duplicateCount++;
			System.out
					.println("\tDuplicate " + jiraTicket.getTicketType().name() + " notification (rule: "
							+ jiraTicket.getRuleName() + "): The notification at \n\t\t" + jiraTicket.getDateCreated()
							+ " is a duplicate of notification at \n\t\t"
							+ tickets.get(jiraTicket.hashCode()).getDateCreated());
		} else {
			tickets.put(jiraTicket.hashCode(), jiraTicket);
		}
	}

	private BomComponentVersionPolicyStatus getCompPolicyStatusFromLink(String compPolicyStatusLink) throws Exception {

		if (compPolicyStatusLink == null) {
			return null;
		}

		BomComponentVersionPolicyStatus policyStatus = null;
		try {
			policyStatus = svc.getResourceFromAbsoluteUrl(BomComponentVersionPolicyStatus.class, compPolicyStatusLink);
		} catch (NotificationServiceException e) {
			log.warn("Error getting policy status from " + compPolicyStatusLink + ": " + e.getMessage()
					+ "; This component was probably removed from this BOM");
			return null;
		}

		return policyStatus;
	}

	private void processRuleViolationNotification(Date notificationTimeStamp,
			RuleViolationNotificationItem ruleViolationNotif) throws Exception {
		List<ComponentVersionStatus> compStatuses = ruleViolationNotif.getContent().getComponentVersionStatuses();
		for (ComponentVersionStatus compStatus : compStatuses) {
			log.debug(compStatus.toString());
			String componentVersion;
			try {
				componentVersion = getComponentVersionNameFromLink(compStatus.getComponentVersionLink());
			} catch (Exception e) {
				componentVersion = "<not specified>";
			}

			processPolicyViolation(notificationTimeStamp, ruleViolationNotif,
					compStatus.getBomComponentVersionPolicyStatusLink(), ruleViolationNotif.getContent()
							.getProjectName(), ruleViolationNotif.getContent().getProjectVersionName(),
					compStatus.getComponentName(), componentVersion);
		}
	}

	private String getComponentVersionNameFromLink(String componentVersionLink) throws NotificationServiceException {
		if (componentVersionLink == null) {
			return "<null>";
		}

		ComponentVersion componentVersion = svc.getLinkedResourceFromAbsoluteUrl(ComponentVersion.class,
				componentVersionLink);
		return componentVersion.getVersionName();
	}

	private void processPolicyViolation(Date notificationTimeStamp,
			RuleViolationNotificationItem ruleViolationNotificationItem, String policyStatusLink, String projectName,
			String projectVersion, String componentName, String componentVersion) throws Exception {

		try {
			ApprovalStatusItem policyStatus = svc.getLinkedResourceFromAbsoluteUrl(ApprovalStatusItem.class,
					policyStatusLink);
			log.info("Approval Status: " + policyStatus);
			if (PolicyStatus.NOT_IN_VIOLATION.equals(policyStatus.getApprovalStatus())) {
				log.info("Not generating a ticket");
				return; // don't need a ticket if it's not in violation
			}
			processPolicy(notificationTimeStamp, ruleViolationNotificationItem, policyStatus.getLink("policy-rule"),
					projectName, projectVersion, componentName, componentVersion);
		} catch (NotificationServiceException e) {
			log.warn("Error getting policy status from " + policyStatusLink + ": " + e.getMessage()
					+ "; This component was probably removed from this BOM");
		}
	}

	private void processPolicy(Date notificationTimeStamp, RuleViolationNotificationItem ruleViolationNotificationItem,
			String policyLink, String projectName, String projectVersion, String componentName, String componentVersion)
			throws IOException, URISyntaxException {
		if (policyLink == null) {
			log.warn("Policy link is null in notification item: " + ruleViolationNotificationItem);

			JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp, JiraTicketType.RULE_VIOLATION, projectName,
					projectVersion, componentName, componentVersion, null, null, ActionRequired.REVIEW);
			System.out.println(jiraTicket);
			addToMap(jiraTicket);
			return;
		}

		try {
			PolicyRule policyRule = svc.getResourceFromAbsoluteUrl(PolicyRule.class, policyLink);
			JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp, JiraTicketType.RULE_VIOLATION, projectName,
					projectVersion, componentName, componentVersion, null, policyRule.getName(), ActionRequired.REVIEW);
			System.out.println(jiraTicket);
			addToMap(jiraTicket);
		} catch (NotificationServiceException e) {
			log.error("Error getting violated rule name");
		}
	}

	private void processVulnerabilityNotification(Date notificationTimeStamp, VulnerabilityNotificationItem vulnNotif)
			throws IOException, BDRestException, URISyntaxException, NotificationServiceException {
		log.debug("processVulnerabilityNotification()");

		if (vulnNotif.getContent().getAffectedProjectVersions() == null) {
			log.error("This vulnerability notification has no affected project versions; this might mean it's an old notification: "
					+ vulnNotif);
			return;
		}
		for (ProjectVersion affectedProjectVersion : vulnNotif.getContent().getAffectedProjectVersions()) {
			processProjectVersionLink(notificationTimeStamp, affectedProjectVersion.projectVersionLink(),
					affectedProjectVersion.getProjectName(), vulnNotif.getContent().getComponentName(), vulnNotif
							.getContent().getVersionName());
		}
	}

	private void processProjectVersionLink(Date notificationTimeStamp, String projectVersionLink, String projectName,
			String targetComponentName, String targetComponentVersion) throws URISyntaxException, IOException,
			NotificationServiceException {

		ProjectVersionItem versionItem = svc.getLinkedResourceFromAbsoluteUrl(ProjectVersionItem.class,
				projectVersionLink);
		log.info(versionItem.toString()); // TODO change to debug
		String vulnerableComponentsLink = versionItem.getLink("vulnerable-components");
		log.debug("Link to vulnerable components: " + vulnerableComponentsLink);
		processVulnerableComponentsLink(notificationTimeStamp, vulnerableComponentsLink, projectName,
				versionItem.getVersionName(), targetComponentName, targetComponentVersion);
	}

	private void processVulnerableComponentsLink(Date notificationTimeStamp, String vulnerableComponentsLink,
			String projectName, String projectVersionName, String targetComponentName, String targetComponentVersion)
			throws URISyntaxException, IOException, NotificationServiceException {

		VulnerableComponentsResponse vulnCompsResponse = svc.getResourceFromAbsoluteUrl(
				VulnerableComponentsResponse.class, vulnerableComponentsLink);

		for (VulnerableComponentItem vulnerableComponentItem : vulnCompsResponse.getItems()) {
			log.debug(vulnerableComponentItem.toString());

			if (!vulnerableComponentItem.getComponentName().equals(targetComponentName)) {
				log.debug("Wrong component name; skipping this one");
				continue;
			}
			if (!vulnerableComponentItem.getComponentVersionName().equals(targetComponentVersion)) {
				log.debug("Wrong component version; skipping this one");
				continue;
			}
			if (RemediationStatus.REMEDIATION_REQUIRED.equals(vulnerableComponentItem.getVulnerabilityWithRemediation()
					.getRemediationStatus())) {

				JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp, JiraTicketType.VULNERABILITY,
						projectName, projectVersionName, targetComponentName, targetComponentVersion,
						vulnerableComponentItem.getVulnerabilityWithRemediation().getVulnerabilityName(), null,
						ActionRequired.REMEDIATION);
				System.out.println(jiraTicket);
				addToMap(jiraTicket);
			}
			if (RemediationStatus.NEEDS_REVIEW.equals(vulnerableComponentItem.getVulnerabilityWithRemediation()
					.getRemediationStatus())) {

				JiraTicket jiraTicket = new JiraTicket(notificationTimeStamp, JiraTicketType.VULNERABILITY,
						projectName, projectVersionName, targetComponentName, targetComponentVersion,
						vulnerableComponentItem.getVulnerabilityWithRemediation().getVulnerabilityName(), null,
						ActionRequired.REVIEW);
				System.out.println(jiraTicket);
				addToMap(jiraTicket);
			}
		}
	}
}
