package com.movieflix.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer actorId;

    @Column(nullable = false, length = 200)
    private String name; // Tên diễn viên

    @Column(nullable = false, length = 200)
    private String characterName; // Tên nhân vật trong phim

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie; // Mối quan hệ với Movie, mỗi diễn viên thuộc một phim
}
