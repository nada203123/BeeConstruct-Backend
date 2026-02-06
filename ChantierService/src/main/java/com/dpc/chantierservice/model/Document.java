package com.dpc.chantierservice.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table ( name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long chantierId;

    @Column(name = "fileName",length = 512)
    private String fileName;

    @Column(name = "file_path",length = 1024)
    private String filePath;



    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_contract", nullable = false)
    private boolean isContract = false;

}
