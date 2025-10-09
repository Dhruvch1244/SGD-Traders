package com.example.demo.models;

// Lombok imports removed
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

// Removed Lombok. Added explicit getters/setters.
@Table("CLIENT_IDENTIFICATIONS")
public class ClientIdentification implements Persistable<String> {
    @Id
    @Column("ID")
    private String id;
    @Column("CLIENTID")
    private String clientId;
    @Column("TYPE")
    private String type;
    @Column("VALUE")
    private String value;

    @Transient
    private boolean isNew = true;

    public ClientIdentification() {}

    public ClientIdentification(String id, String clientId, String type, String value, boolean isNew) {
        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.value = value;
        this.isNew = isNew;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    @Override
    public boolean isNew() { return this.isNew; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
    public void setAsNotNew() { this.isNew = false; }
}
