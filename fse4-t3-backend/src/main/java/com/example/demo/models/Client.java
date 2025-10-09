package com.example.demo.models;

// Lombok imports removed
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

// Removed Lombok. Added explicit getters/setters.
@Table("CLIENTS")
public class Client implements Persistable<String> {
    @Id
    @Column("CLIENTID")
    private String clientId;
    @Column("NAME")
    private String name;
    @Column("EMAIL")
    private String email;
    @Column("DATEOFBIRTH")
    private String dateOfBirth;
    @Column("COUNTRY")
    private String country;
    @Column("POSTALCODE")
    private String postalCode;
    @Column("PASSWORD")
    private String password;

    @Transient
    private boolean isNew = true;

    public Client() {}

    public Client(String clientId, String name, String email, String dateOfBirth, String country, String postalCode, String password, boolean isNew) {
        this.clientId = clientId;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.country = country;
        this.postalCode = postalCode;
        this.password = password;
        this.isNew = isNew;
    }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    @Override
    public boolean isNew() { return this.isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
    @Override
    public String getId() { return this.clientId; }
    public void setAsNotNew() { this.isNew = false; }
}
