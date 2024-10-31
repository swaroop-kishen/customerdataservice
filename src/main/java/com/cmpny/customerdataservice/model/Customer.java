package com.cmpny.customerdataservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Java object wrapper to model Customer information
 * to and from the Database
 */
@Entity
@Table
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private UUID id; // primary key
    @Column(nullable = false)
    private String firstName;
    @Column()
    private String middleName;
    @Column(nullable = false)
    private String lastName;
    @Column(unique = true, nullable = false)
    private String emailAddress; // unique
    @Column(nullable = false)
    private String phoneNumber;

}
