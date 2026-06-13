package proyecto_laboS.labo.service;

public interface EmailService {

    boolean enviarRecuperacionPassword(String destinatario, String token);
}
