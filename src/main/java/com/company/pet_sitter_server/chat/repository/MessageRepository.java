package com.company.pet_sitter_server.chat.repository;

import com.company.pet_sitter_server.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    //  ดึงประวัติแชทระหว่างคน 2 คน (เรียงตามเวลา)
    @Query("SELECT m FROM Message m WHERE (m.senderId = :user1 AND m.receiverId = :user2) OR (m.senderId = :user2 AND m.receiverId = :user1) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(@Param("user1") Long user1, @Param("user2") Long user2);

    // 🟢 ดึงข้อความทั้งหมดที่ตัวเราส่ง หรือ ตัวเราเป็นคนรับ (เรียงจากใหม่สุดไปเก่าสุด)
    @Query("SELECT m FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId ORDER BY m.createdAt DESC")
    List<Message> findAllUserMessages(@Param("userId") Long userId);

    // 🟢 เปลี่ยนสถานะข้อความที่อีกฝ่ายส่งมาหาเรา ให้เป็น "อ่านแล้ว"
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true WHERE m.senderId = :partnerId AND m.receiverId = :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("userId") Long userId, @Param("partnerId") Long partnerId);

}