package com.isa.backend.repository;

import com.isa.backend.model.VideoPost;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoPostRepository extends JpaRepository<VideoPost, Long>{

    @Transactional
    @Modifying
    @Query("UPDATE VideoPost v SET v.views = v.views + 1 WHERE v.id = :id")
    void incrementViews(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query("UPDATE VideoPost v SET v.trendingScore = :score WHERE v.id = :id")
    void updateTrendingScore(@Param("id") Long id, @Param("score") Double score);

    @Query("SELECT v FROM VideoPost v " +
            "WHERE dwithin(v.location.coordinates, :userLocation, :radius) = true " +
            "ORDER BY v.trendingScore DESC")
    List<VideoPost> findTrendingInRadius(@Param("userLocation") org.locationtech.jts.geom.Point userLocation,
                                         @Param("radius") Double radiusInMeters);
}
