-- 알림 단계별 수동 검증용 데이터입니다.
-- 회원과 활성 회원권 상품이 이미 있는 개발 DB에서만 실행하세요.
-- 실행 전 @member_id와 @product_id가 올바른지 반드시 확인합니다.

SET @member_id = (SELECT id FROM members WHERE status = 'ACTIVE' ORDER BY id LIMIT 1);
SET @product_id = (SELECT id FROM membership_products WHERE status = 'ACTIVE' ORDER BY id LIMIT 1);

INSERT INTO member_memberships
    (member_id, product_id, start_date, end_date, remaining_pt_sessions, status)
VALUES
    (@member_id, @product_id, CURRENT_DATE - INTERVAL 23 DAY, CURRENT_DATE + INTERVAL 7 DAY, 0, 'ACTIVE'),
    (@member_id, @product_id, CURRENT_DATE - INTERVAL 27 DAY, CURRENT_DATE + INTERVAL 3 DAY, 0, 'ACTIVE'),
    (@member_id, @product_id, CURRENT_DATE - INTERVAL 30 DAY, CURRENT_DATE, 0, 'ACTIVE'),
    (@member_id, @product_id, CURRENT_DATE - INTERVAL 31 DAY, CURRENT_DATE - INTERVAL 1 DAY, 0, 'EXPIRED');

-- 화면 요청 시 notifications가 생성됩니다.
-- 확인 경로: /member/notifications, /admin/notifications
