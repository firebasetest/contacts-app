@Service
@Transactional
public class ContactService {

    private final ContactRepository repository;

    public ContactService(ContactRepository repository) { this.repository = repository; }

    public Contact createContact(Contact contact) {
        contact.setValidFrom(LocalDateTime.now());
        return repository.save(contact);
    }

    // Temporal "Update": Close old version, create new version
    public void updateContact(UUID id, Contact newVersion) {
        Contact oldVersion = repository.findById(id).orElseThrow();
        oldVersion.setValidTo(LocalDateTime.now());
        repository.save(oldVersion);

        newVersion.setValidFrom(LocalDateTime.now());
        repository.save(newVersion);
    }
}