@EventListener
public void handlePartnerInactivity(InactivityEvent event) {
    Contact contact = contactRepository.findById(event.getContactId());
    contact.setStatus(Status.INACTIVE);
    contact.setLastModifiedDate(LocalDateTime.now());
    contactRepository.save(contact);
    
    // Notify the BU Admin
    notificationService.sendAlert(contact.getBuAdminEmail(), "Partner account inactive");
}