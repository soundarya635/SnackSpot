package com.example.snackspotApi.Repository;

import com.example.snackspotApi.Entity.Order;
import com.example.snackspotApi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.food = NULL WHERE oi.food.id = :foodId")
    void nullifyFoodReferenceInOrderItems(@Param("foodId") Long foodId);
}
