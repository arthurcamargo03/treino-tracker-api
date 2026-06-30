package com.treinotracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "series")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sessao_exercicio_id", nullable = false)
    private SessaoExercicio sessaoExercicio;

    @Positive
    @Column(nullable = false)
    private int posicao;

    @Positive
    @Column(nullable = false)
    private double carga;

    @Positive
    @Column(nullable = false)
    private int reps;

    public Serie(SessaoExercicio sessaoExercicio, int posicao, double carga, int reps) {
        this.sessaoExercicio = sessaoExercicio;
        this.posicao = posicao;
        this.carga = carga;
        this.reps = reps;
    }

    public double estimated1RM() {
        return carga * (1 + reps / 30.0);
    }
}
