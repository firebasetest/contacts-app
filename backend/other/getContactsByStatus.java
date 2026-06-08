@GetMapping
public List<Contact> getContactsByStatus(@RequestParam String status) {
    // The RLS layer automatically handles the business_unit_id filtering
    return contactRepository.findByStatus(status);
}