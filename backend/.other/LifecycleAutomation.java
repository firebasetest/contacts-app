@Component
public class LifecycleAutomation {

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1 AM
    public void processDataRetention() {
        // 1. Identify contacts that passed the retention date
        List<Contact> expiredContacts = contactRepository.findExpiredContacts(RetentionPeriod.YEARS_7);
        
        // 2. Perform GDPR-compliant deletion
        for (Contact contact : expiredContacts) {
            // Anonymize personal info (First/Last Name, Email)
            contact.anonymize();
            contact.setStatus(Status.ARCHIVED);
            contactRepository.save(contact);
        }
    }
}