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

    @Query(value = "SELECT v.* FROM video_posts v " +
            "JOIN locations l ON v.location_id = l.id " +
            "WHERE ST_DWithin(CAST(l.coordinates AS geography), CAST(:userLocation AS geography), :radius) = true " +
            "ORDER BY v.trending_score DESC",
            nativeQuery = true)
    List<VideoPost> findTrendingInRadius(@Param("userLocation") org.locationtech.jts.geom.Point userLocation,
                                         @Param("radius") Double radiusInMeters);
}
