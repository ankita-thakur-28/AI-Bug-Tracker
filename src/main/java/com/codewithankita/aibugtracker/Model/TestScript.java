package com.codewithankita.aibugtracker.Model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_scripts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestScript {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id", referencedColumnName = "id", nullable = false, unique = true)
    private Bug bug;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestScriptStatus status;

    @Column(columnDefinition = "TEXT")
    private String logs;

    private LocalDateTime executedAt;
}
