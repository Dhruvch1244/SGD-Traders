package com.example.demo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("INVESTMENT_PURPOSES")
public class InvestmentPurpose {
    @Id
    @Column("ID")
    private Long id;
    @Column("PURPOSE")
    private String purpose;
}
