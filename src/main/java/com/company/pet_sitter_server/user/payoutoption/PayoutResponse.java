package com.company.pet_sitter_server.user.payoutoption;

public class PayoutResponse {
    public Long id;          // Transaction No. (booking id)
    public String date;      // วันที่ (created_at)
    public Long fromUserId;  // user_id ของผู้จอง (From)
    public String fromName;  // ชื่อของผู้จอง (จาก UserProfile)
    public Double amount;    // total_price (Amount)
}
