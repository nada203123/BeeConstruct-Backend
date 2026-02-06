package com.example.offreservice.specifications;

import com.example.offreservice.model.Offre;
import com.example.offreservice.model.StatutOffre;
import com.example.offreservice.model.TypeOffre;
import org.springframework.data.jpa.domain.Specification;

public class OffreSpecifications {
    public static Specification<Offre> withClientId(Long clientId) {
        return (root, query, cb) ->
                clientId == null ? null : cb.equal(root.get("clientId"), clientId);
    }

    public static Specification<Offre> withLocalisation(String localisation) {
        return (root, query, cb) ->
                localisation == null ? null : cb.equal(root.get("localisation"), localisation);
    }

    public static Specification<Offre> withStatut(StatutOffre statut) {
        return (root, query, cb) ->
                statut == null ? null : cb.equal(root.get("statutOffre"), statut);
    }

    // Add this to OffreSpecifications.java
    public static Specification<Offre> withArchived(boolean archived) {
        return (root, query, cb) -> cb.equal(root.get("archived"), archived);
    }
    public static Specification<Offre> withType(TypeOffre type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }
}
