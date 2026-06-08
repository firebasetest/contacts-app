@RestController
@RequestMapping("/api/v1/contacts")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) { this.contactService = contactService; }

    @PostMapping
    public ResponseEntity<Contact> create(@RequestBody Contact contact) {
        return ResponseEntity.ok(contactService.createContact(contact));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody Contact contact) {
        contactService.updateContact(id, contact);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    public List<Contact> search(@RequestParam String filter) {
        return contactService.findByCustomAttributes(filter);
    }
}