package org.gimuemoa.minicbs.model;

import jakarta.persistence.*;
import lombok.*;
import org.gimuemoa.minicbs.model.enums.EnumRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_role")
public class AppRole{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rolename")
    private EnumRole roleName;

}
