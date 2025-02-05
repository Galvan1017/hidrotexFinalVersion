public class Cliente {

    private String name;

    private String mail;

    private String phone;

    private String rfc;

    private boolean activo;

    public Cliente(String name, String mail, String phone) {

        this.name = name;
        this.mail = mail;
        this.phone = phone;
        activo = true;
    }

    public Cliente(String name, String mail, String phone, String rfc) {

        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.rfc = rfc;
    }

    public Cliente(String name, String mail, String phone, String rfc, boolean activo) {

        this.name = name;
        this.mail = mail;
        this.phone = phone;
        this.rfc = rfc;
        this.activo = activo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getRfc() {
        return rfc;
    }

    public void setRfc(String rfc) {
        this.rfc = rfc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        if (!mail.contains("@") || (!mail.contains("."))) {

        } else {
            this.mail = mail;
        }

    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        boolean isNumeric = phone.matches("^[0-9]+$");
        if (isNumeric) {
            this.phone = phone;
        } else {

        }

    }

}
