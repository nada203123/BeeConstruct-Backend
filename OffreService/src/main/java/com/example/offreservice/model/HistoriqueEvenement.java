package com.example.offreservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historiqueEvenement")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HistoriqueEvenement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @Column(name = "COMMENT")
    private String comment;

    @Column(name = "FILE_PATH",length = 2000)
    private String filePath;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(name = "original_file_name",length = 2000)
    private String originalFileName;

    @Column(name = "IS_ARCHIVED", nullable = false)
    private boolean isArchived = false;


}
