-- Gymrovia 로컬 개발·포트폴리오 시연용 종합 데이터
-- Flyway가 적용된 비어 있는 전용 DB에서 한 번만 실행합니다.
-- 기존/운영 DB에는 실행하지 않습니다. 모든 로컬 계정 비밀번호: password
SET @today = CURDATE();
SET @now = NOW();
SET @pw = '$2a$10$vPQkIMlwttHC0vi1qqgBU.tTE105UeV7WryKN/LefQk3P9c8v1iHm';
START TRANSACTION;

-- 관리자 1명, 트레이너 3명, 회원 10명
INSERT INTO users (id,email,role,status,last_login_at,created_at) VALUES
(1,'admin@fitflow.local','ADMIN','ACTIVE',DATE_SUB(@now,INTERVAL 10 MINUTE),DATE_SUB(@now,INTERVAL 180 DAY)),
(2,'trainer01@fitflow.local','TRAINER','ACTIVE',DATE_SUB(@now,INTERVAL 30 MINUTE),DATE_SUB(@now,INTERVAL 160 DAY)),
(3,'trainer02@fitflow.local','TRAINER','ACTIVE',DATE_SUB(@now,INTERVAL 1 DAY),DATE_SUB(@now,INTERVAL 150 DAY)),
(4,'trainer03@fitflow.local','TRAINER','ACTIVE',DATE_SUB(@now,INTERVAL 30 DAY),DATE_SUB(@now,INTERVAL 140 DAY)),
(5,'member01@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 1 HOUR),DATE_SUB(@now,INTERVAL 120 DAY)),
(6,'member02@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 3 HOUR),DATE_SUB(@now,INTERVAL 100 DAY)),
(7,'member03@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 1 DAY),DATE_SUB(@now,INTERVAL 80 DAY)),
(8,'member04@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 4 DAY),DATE_SUB(@now,INTERVAL 70 DAY)),
(9,'member05@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 7 DAY),DATE_SUB(@now,INTERVAL 60 DAY)),
(10,'member06@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 2 DAY),DATE_SUB(@now,INTERVAL 50 DAY)),
(11,'member07@fitflow.local','MEMBER','SUSPENDED',DATE_SUB(@now,INTERVAL 20 DAY),DATE_SUB(@now,INTERVAL 45 DAY)),
(12,'member08@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 2 HOUR),DATE_SUB(@now,INTERVAL 35 DAY)),
(13,'member09@fitflow.local','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 6 HOUR),DATE_SUB(@now,INTERVAL 20 DAY)),
(14,'member.google.demo@gmail.com','MEMBER','ACTIVE',DATE_SUB(@now,INTERVAL 1 DAY),DATE_SUB(@now,INTERVAL 10 DAY));

INSERT INTO user_local_credentials (user_id,login_id,password_hash) VALUES
(1,'admin',@pw),(2,'trainer01',@pw),(3,'trainer02',@pw),(4,'trainer03',@pw),
(5,'member01',@pw),(6,'member02',@pw),(7,'member03',@pw),(8,'member04',@pw),
(9,'member05',@pw),(10,'member06',@pw),(11,'member07',@pw),(12,'member08',@pw),(13,'member09',@pw);
INSERT INTO user_oauth_accounts (id,user_id,provider,provider_subject,provider_email) VALUES
(1,14,'GOOGLE','fitflow-demo-google-subject','member.google.demo@gmail.com');

INSERT INTO trainers (id,user_id,name,phone,specialty,status) VALUES
(1,2,'김도윤','010-2100-1001','근력 향상·자세 교정','ACTIVE'),
(2,3,'박서현','010-2100-1002','체중 감량·기능성 운동','ACTIVE'),
(3,4,'이준호','010-2100-1003','재활 운동·스트레칭','INACTIVE');
INSERT INTO members (id,user_id,name,phone,birth_date,gender,trainer_requested,joined_at,status) VALUES
(1,5,'김지훈','010-3100-1001','1994-08-17','MALE',FALSE,DATE_SUB(@today,INTERVAL 120 DAY),'ACTIVE'),
(2,6,'이서연','010-3100-1002','1997-03-21','FEMALE',FALSE,DATE_SUB(@today,INTERVAL 100 DAY),'ACTIVE'),
(3,7,'박민수','010-3100-1003','1991-11-02','MALE',FALSE,DATE_SUB(@today,INTERVAL 80 DAY),'ACTIVE'),
(4,8,'최유진','010-3100-1004','1996-05-14','FEMALE',TRUE,DATE_SUB(@today,INTERVAL 70 DAY),'ACTIVE'),
(5,9,'정현우','010-3100-1005','1989-12-30','MALE',FALSE,DATE_SUB(@today,INTERVAL 60 DAY),'ACTIVE'),
(6,10,'한소희','010-3100-1006','1999-07-08','FEMALE',TRUE,DATE_SUB(@today,INTERVAL 50 DAY),'ACTIVE'),
(7,11,'윤태호','010-3100-1007','1993-02-11','MALE',FALSE,DATE_SUB(@today,INTERVAL 45 DAY),'SUSPENDED'),
(8,12,'강민지','010-3100-1008','1998-10-25','FEMALE',FALSE,DATE_SUB(@today,INTERVAL 35 DAY),'ACTIVE'),
(9,13,'오세진','010-3100-1009','1990-06-19','MALE',FALSE,DATE_SUB(@today,INTERVAL 20 DAY),'ACTIVE'),
(10,14,'송하늘','010-3100-1010','2000-01-07','FEMALE',TRUE,DATE_SUB(@today,INTERVAL 10 DAY),'ACTIVE');
INSERT INTO trainer_assignments (id,member_id,trainer_id,status,started_at,ended_at,assigned_by) VALUES
(1,1,1,'ACTIVE',DATE_SUB(@today,INTERVAL 110 DAY),NULL,1),(2,2,1,'ACTIVE',DATE_SUB(@today,INTERVAL 90 DAY),NULL,1),
(3,3,2,'ACTIVE',DATE_SUB(@today,INTERVAL 70 DAY),NULL,1),(4,4,2,'ACTIVE',DATE_SUB(@today,INTERVAL 60 DAY),NULL,1),
(5,5,3,'ENDED',DATE_SUB(@today,INTERVAL 55 DAY),DATE_SUB(@today,INTERVAL 15 DAY),1),
(6,8,1,'ACTIVE',DATE_SUB(@today,INTERVAL 30 DAY),NULL,1),(7,9,2,'ACTIVE',DATE_SUB(@today,INTERVAL 15 DAY),NULL,1);

INSERT INTO membership_products (id,name,product_type,duration_days,price,pt_session_count,status) VALUES
(1,'1개월 자유 이용권','GYM',30,80000,0,'ACTIVE'),(2,'3개월 자유 이용권','GYM',90,180000,0,'ACTIVE'),
(3,'6개월 자유 이용권','GYM',180,320000,0,'ACTIVE'),(4,'PT 10회 패키지','PT',120,500000,10,'ACTIVE'),
(5,'헬스 3개월 + PT 5회','COMBINED',90,390000,5,'ACTIVE'),(6,'오픈 기념 1개월권','GYM',30,50000,0,'INACTIVE');
INSERT INTO member_memberships (id,member_id,product_id,start_date,end_date,remaining_pt_sessions,status) VALUES
(1,1,2,DATE_SUB(@today,INTERVAL 30 DAY),DATE_ADD(@today,INTERVAL 59 DAY),0,'ACTIVE'),
(2,1,4,DATE_SUB(@today,INTERVAL 25 DAY),DATE_ADD(@today,INTERVAL 94 DAY),6,'ACTIVE'),
(3,2,1,DATE_SUB(@today,INTERVAL 18 DAY),DATE_ADD(@today,INTERVAL 11 DAY),0,'ACTIVE'),
(4,2,1,DATE_ADD(@today,INTERVAL 12 DAY),DATE_ADD(@today,INTERVAL 41 DAY),0,'ACTIVE'),
(5,3,5,DATE_SUB(@today,INTERVAL 20 DAY),DATE_ADD(@today,INTERVAL 69 DAY),4,'ACTIVE'),
(6,4,1,DATE_ADD(@today,INTERVAL 5 DAY),DATE_ADD(@today,INTERVAL 34 DAY),0,'PENDING_PAYMENT'),
(7,5,2,DATE_SUB(@today,INTERVAL 100 DAY),DATE_SUB(@today,INTERVAL 11 DAY),0,'EXPIRED'),
(8,6,3,DATE_SUB(@today,INTERVAL 40 DAY),DATE_ADD(@today,INTERVAL 139 DAY),0,'PAUSED'),
(9,7,1,DATE_SUB(@today,INTERVAL 15 DAY),DATE_ADD(@today,INTERVAL 14 DAY),0,'CANCELLED'),
(10,8,4,DATE_SUB(@today,INTERVAL 10 DAY),DATE_ADD(@today,INTERVAL 109 DAY),9,'ACTIVE'),
(11,9,1,DATE_SUB(@today,INTERVAL 24 DAY),DATE_ADD(@today,INTERVAL 5 DAY),0,'ACTIVE'),
(12,10,2,DATE_SUB(@today,INTERVAL 7 DAY),DATE_ADD(@today,INTERVAL 82 DAY),0,'ACTIVE'),
(13,3,1,DATE_SUB(@today,INTERVAL 70 DAY),DATE_SUB(@today,INTERVAL 41 DAY),0,'EXPIRED'),
(14,5,1,DATE_ADD(@today,INTERVAL 1 DAY),DATE_ADD(@today,INTERVAL 30 DAY),0,'PENDING_PAYMENT'),
(15,6,1,DATE_ADD(@today,INTERVAL 3 DAY),DATE_ADD(@today,INTERVAL 32 DAY),0,'CANCELLED'),
(16,10,4,DATE_ADD(@today,INTERVAL 2 DAY),DATE_ADD(@today,INTERVAL 121 DAY),10,'PENDING_PAYMENT');

INSERT INTO payments (id,member_id,member_membership_id,amount,payment_method,status,paid_at) VALUES
(1,1,1,180000,'EASY_PAY','COMPLETED',DATE_SUB(@now,INTERVAL 30 DAY)),
(2,1,2,500000,'CARD','PARTIALLY_REFUNDED',DATE_SUB(@now,INTERVAL 25 DAY)),
(3,2,3,80000,'TRANSFER','COMPLETED',DATE_SUB(@now,INTERVAL 18 DAY)),
(4,2,4,80000,'CARD','COMPLETED',DATE_SUB(@now,INTERVAL 1 DAY)),
(5,3,5,390000,'CARD','COMPLETED',DATE_SUB(@now,INTERVAL 20 DAY)),
(6,5,7,180000,'CASH','COMPLETED',DATE_SUB(@now,INTERVAL 100 DAY)),
(7,6,8,320000,'TRANSFER','COMPLETED',DATE_SUB(@now,INTERVAL 40 DAY)),
(8,7,9,80000,'CARD','REFUNDED',DATE_SUB(@now,INTERVAL 15 DAY)),
(9,8,10,500000,'EASY_PAY','COMPLETED',DATE_SUB(@now,INTERVAL 10 DAY)),
(10,9,11,80000,'CASH','COMPLETED',DATE_SUB(@now,INTERVAL 24 DAY)),
(11,10,12,180000,'CARD','COMPLETED',@now);
-- 아래 payment_key는 화면 시연용이며 실제 Toss 취소 API에서 사용할 수 없습니다.
INSERT INTO payment_orders (id,order_id,member_id,member_membership_id,payment_id,amount,status,payment_key,idempotency_key,failure_code,failure_message,expires_at,approved_at,compensation_idempotency_key,compensation_transaction_key,compensation_at,created_at) VALUES
(1,'DEMO-PAID-0001',1,1,1,180000,'PAID','demo_payment_key_0001','demo-approve-0001',NULL,NULL,DATE_SUB(@now,INTERVAL 29 DAY),DATE_SUB(@now,INTERVAL 30 DAY),NULL,NULL,NULL,DATE_SUB(@now,INTERVAL 30 DAY)),
(2,'DEMO-PAID-0002',8,10,9,500000,'PAID','demo_payment_key_0002','demo-approve-0002',NULL,NULL,DATE_SUB(@now,INTERVAL 9 DAY),DATE_SUB(@now,INTERVAL 10 DAY),NULL,NULL,NULL,DATE_SUB(@now,INTERVAL 10 DAY)),
(3,'DEMO-FAILED-0001',4,6,NULL,80000,'FAILED',NULL,'demo-failed-0001','PAY_PROCESS_CANCELED','사용자가 결제를 취소했습니다.',DATE_ADD(@now,INTERVAL 20 MINUTE),NULL,NULL,NULL,NULL,DATE_SUB(@now,INTERVAL 1 DAY)),
(4,'DEMO-EXPIRED-0001',5,14,NULL,80000,'EXPIRED',NULL,'demo-expired-0001','ORDER_EXPIRED','결제 가능 시간이 만료되었습니다.',DATE_SUB(@now,INTERVAL 1 DAY),NULL,NULL,NULL,NULL,DATE_SUB(@now,INTERVAL 2 DAY)),
(5,'DEMO-COMPENSATED-0001',6,15,NULL,80000,'COMPENSATED','demo_payment_key_compensated','demo-compensated-0001','LOCAL_SAVE_FAILED','로컬 저장 실패 후 자동 취소되었습니다.',DATE_SUB(@now,INTERVAL 2 DAY),DATE_SUB(@now,INTERVAL 3 DAY),'demo-compensate-0001','demo-cancel-tx-0001',DATE_SUB(@now,INTERVAL 3 DAY),DATE_SUB(@now,INTERVAL 3 DAY)),
(6,'DEMO-READY-0001',10,16,NULL,500000,'READY',NULL,'demo-ready-0001',NULL,NULL,DATE_ADD(@now,INTERVAL 30 MINUTE),NULL,NULL,NULL,NULL,@now);
INSERT INTO refunds (id,payment_id,amount,reason,status,refunded_at,processed_by,failure_code,failure_message,created_at) VALUES
(1,2,50000,'PT 1회 미사용분 부분 환불','COMPLETED',DATE_SUB(@now,INTERVAL 12 DAY),1,NULL,NULL,DATE_SUB(@now,INTERVAL 12 DAY)),
(2,8,80000,'회원 요청에 따른 전체 환불','COMPLETED',DATE_SUB(@now,INTERVAL 13 DAY),1,NULL,NULL,DATE_SUB(@now,INTERVAL 13 DAY)),
(3,7,30000,'프로모션 상품 환불 요청','REJECTED',NULL,1,'REFUND_POLICY','환불 가능 기간이 지났습니다.',DATE_SUB(@now,INTERVAL 5 DAY));

INSERT INTO reservations (id,member_id,trainer_id,customer_name,customer_phone,reservation_type,status,starts_at,ends_at,memo,cancellation_reason,created_by,created_at) VALUES
(1,1,1,'김지훈','010-3100-1001','REGULAR_PT','CONFIRMED',TIMESTAMP(@today,'10:00:00'),TIMESTAMP(@today,'11:00:00'),'하체 집중 수업',NULL,1,DATE_SUB(@now,INTERVAL 3 DAY)),
(2,3,2,'박민수','010-3100-1003','REGULAR_PT','CONFIRMED',TIMESTAMP(@today,'14:00:00'),TIMESTAMP(@today,'15:00:00'),'상체 자세 점검',NULL,1,DATE_SUB(@now,INTERVAL 2 DAY)),
(3,4,1,'최유진','010-3100-1004','TRIAL_PT','PENDING',TIMESTAMP(DATE_ADD(@today,INTERVAL 1 DAY),'13:00:00'),TIMESTAMP(DATE_ADD(@today,INTERVAL 1 DAY),'14:00:00'),'PT 체험 희망',NULL,1,@now),
(4,NULL,2,'임재현','010-3999-1001','CONSULTATION','PENDING',TIMESTAMP(DATE_ADD(@today,INTERVAL 1 DAY),'16:00:00'),TIMESTAMP(DATE_ADD(@today,INTERVAL 1 DAY),'16:30:00'),'3개월 회원권 상담',NULL,1,@now),
(5,2,1,'이서연','010-3100-1002','REGULAR_PT','COMPLETED',TIMESTAMP(DATE_SUB(@today,INTERVAL 1 DAY),'18:00:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 1 DAY),'19:00:00'),'등 운동 수업 완료',NULL,1,DATE_SUB(@now,INTERVAL 5 DAY)),
(6,8,1,'강민지','010-3100-1008','REGULAR_PT','CANCELLED',TIMESTAMP(DATE_ADD(@today,INTERVAL 2 DAY),'11:00:00'),TIMESTAMP(DATE_ADD(@today,INTERVAL 2 DAY),'12:00:00'),NULL,'회원 개인 일정',1,DATE_SUB(@now,INTERVAL 1 DAY)),
(7,NULL,2,'고은비','010-3999-1002','CONSULTATION','NO_SHOW',TIMESTAMP(DATE_SUB(@today,INTERVAL 3 DAY),'15:00:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 3 DAY),'15:30:00'),'전화 상담 후 방문 예약',NULL,1,DATE_SUB(@now,INTERVAL 6 DAY)),
(8,9,2,'오세진','010-3100-1009','REGULAR_PT','CONFIRMED',TIMESTAMP(DATE_ADD(@today,INTERVAL 3 DAY),'19:00:00'),TIMESTAMP(DATE_ADD(@today,INTERVAL 3 DAY),'20:00:00'),'코어 운동',NULL,1,@now);

INSERT INTO attendances (id,member_id,attendance_date,checked_in_at,checked_out_at) VALUES
(1,1,DATE_SUB(@today,INTERVAL 12 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 12 DAY),'18:10:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 12 DAY),'19:25:00')),
(2,2,DATE_SUB(@today,INTERVAL 10 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 10 DAY),'07:40:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 10 DAY),'08:50:00')),
(3,3,DATE_SUB(@today,INTERVAL 8 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 8 DAY),'19:00:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 8 DAY),'20:10:00')),
(4,1,DATE_SUB(@today,INTERVAL 6 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 6 DAY),'18:25:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 6 DAY),'19:40:00')),
(5,8,DATE_SUB(@today,INTERVAL 5 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 5 DAY),'10:15:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 5 DAY),'11:20:00')),
(6,9,DATE_SUB(@today,INTERVAL 3 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 3 DAY),'20:00:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 3 DAY),'21:05:00')),
(7,2,DATE_SUB(@today,INTERVAL 1 DAY),TIMESTAMP(DATE_SUB(@today,INTERVAL 1 DAY),'08:05:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 1 DAY),'09:10:00')),
(8,1,@today,TIMESTAMP(@today,'09:10:00'),TIMESTAMP(@today,'10:25:00')),
(9,3,@today,TIMESTAMP(@today,'11:30:00'),NULL),(10,8,@today,TIMESTAMP(@today,'13:20:00'),TIMESTAMP(@today,'14:30:00'));

INSERT INTO workout_routines (id,member_id,trainer_id,title,description,status,start_date,end_date) VALUES
(1,1,1,'4주 근력 향상 프로그램','주 4회 상·하체 분할 루틴','ACTIVE',DATE_SUB(@today,INTERVAL 14 DAY),DATE_ADD(@today,INTERVAL 14 DAY)),
(2,2,1,'체형 교정 기초 루틴','코어 안정성과 자세 교정 중심','ACTIVE',DATE_SUB(@today,INTERVAL 10 DAY),DATE_ADD(@today,INTERVAL 20 DAY)),
(3,3,2,'체지방 감량 서킷','전신 근력과 유산소 복합 루틴','ACTIVE',DATE_SUB(@today,INTERVAL 20 DAY),DATE_ADD(@today,INTERVAL 10 DAY)),
(4,8,1,'PT 입문 프로그램','기본 동작 습득과 가동성 개선','DRAFT',@today,DATE_ADD(@today,INTERVAL 28 DAY)),
(5,9,2,'코어 강화 프로그램','허리 부담을 줄이는 코어 운동','ACTIVE',DATE_SUB(@today,INTERVAL 7 DAY),DATE_ADD(@today,INTERVAL 21 DAY)),
(6,5,NULL,'개인 전신 운동','회원이 직접 진행한 전신 루틴','COMPLETED',DATE_SUB(@today,INTERVAL 40 DAY),DATE_SUB(@today,INTERVAL 10 DAY));
INSERT INTO routine_workout_groups (id,routine_id,title,week_number,day_of_week,display_order) VALUES
(1,1,'하체·코어',1,1,1),(2,1,'등·이두',1,3,2),(3,2,'자세 교정',1,2,1),(4,3,'전신 서킷',1,4,1),
(5,4,'기본 동작',1,1,1),(6,5,'코어 안정화',1,5,1),(7,6,'개인 전신',NULL,6,1);
INSERT INTO routine_exercises (id,routine_id,workout_group_id,exercise_name,day_of_week,display_order,target_sets,target_reps_min,target_reps_max,target_weight,rest_seconds,memo) VALUES
(1,1,1,'백 스쿼트',1,1,4,8,10,60,90,'무릎과 발끝 방향 유지'),(2,1,1,'루마니안 데드리프트',1,2,3,10,12,50,90,'허리 중립 유지'),
(3,1,2,'랫 풀다운',3,1,4,10,12,35,60,NULL),(4,1,2,'덤벨 컬',3,2,3,12,15,8,60,NULL),
(5,2,3,'데드 버그',2,1,3,10,12,NULL,45,'허리가 뜨지 않게 유지'),(6,2,3,'밴드 풀 어파트',2,2,3,15,20,NULL,45,NULL),
(7,3,4,'케틀벨 스윙',4,1,4,15,20,16,45,NULL),(8,3,4,'버피 테스트',4,2,3,10,12,NULL,60,NULL),
(9,4,5,'고블릿 스쿼트',1,1,3,12,15,12,60,NULL),(10,5,6,'플랭크',5,1,3,NULL,NULL,NULL,45,'세트당 40초'),
(11,5,6,'버드독',5,2,3,10,12,NULL,45,NULL),(12,6,7,'푸시업',6,1,4,10,15,NULL,60,NULL);
INSERT INTO workout_sessions (id,member_id,routine_id,started_at,ended_at,memo) VALUES
(1,1,1,TIMESTAMP(DATE_SUB(@today,INTERVAL 6 DAY),'18:30:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 6 DAY),'19:35:00'),'하체 루틴 완료'),
(2,2,2,TIMESTAMP(DATE_SUB(@today,INTERVAL 4 DAY),'08:10:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 4 DAY),'09:00:00'),'코어 안정성 향상'),
(3,3,3,TIMESTAMP(DATE_SUB(@today,INTERVAL 3 DAY),'19:05:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 3 DAY),'20:00:00'),'서킷 3라운드 완료'),
(4,1,1,TIMESTAMP(@today,'09:15:00'),TIMESTAMP(@today,'10:20:00'),'등 운동 수행'),
(5,9,5,TIMESTAMP(DATE_SUB(@today,INTERVAL 1 DAY),'20:05:00'),TIMESTAMP(DATE_SUB(@today,INTERVAL 1 DAY),'20:50:00'),'허리 통증 없이 완료');
INSERT INTO workout_sets (id,session_id,routine_exercise_id,exercise_name,set_number,weight,reps,completed) VALUES
(1,1,1,'백 스쿼트',1,60,10,TRUE),(2,1,1,'백 스쿼트',2,60,10,TRUE),(3,1,1,'백 스쿼트',3,60,9,TRUE),
(4,1,2,'루마니안 데드리프트',1,50,12,TRUE),(5,2,5,'데드 버그',1,NULL,12,TRUE),(6,2,5,'데드 버그',2,NULL,12,TRUE),
(7,3,7,'케틀벨 스윙',1,16,20,TRUE),(8,3,8,'버피 테스트',1,NULL,10,TRUE),(9,4,3,'랫 풀다운',1,35,12,TRUE),
(10,4,3,'랫 풀다운',2,35,11,TRUE),(11,4,4,'덤벨 컬',1,8,15,TRUE),(12,5,10,'플랭크',1,NULL,NULL,TRUE),(13,5,11,'버드독',1,NULL,12,TRUE);

INSERT INTO equipment (id,name,category,location,status,purchased_at) VALUES
(1,'파워 랙 A','웨이트','1층 프리웨이트 존','AVAILABLE','2024-03-10'),(2,'파워 랙 B','웨이트','1층 프리웨이트 존','AVAILABLE','2024-03-10'),
(3,'트레드밀 01','유산소','2층 유산소 존','AVAILABLE','2023-11-20'),(4,'트레드밀 03','유산소','2층 유산소 존','INSPECTION','2023-11-20'),
(5,'레그 프레스','웨이트','1층 머신 존','AVAILABLE','2024-01-15'),(6,'케이블 머신','웨이트','1층 머신 존','REPAIR','2022-09-01'),
(7,'인바디 측정기','측정','1층 상담실','AVAILABLE','2025-02-12'),(8,'스피닝 바이크 02','유산소','2층 GX룸','UNAVAILABLE','2021-06-30');
INSERT INTO equipment_maintenance_logs (id,equipment_id,maintenance_type,description,performed_at,performed_by,next_due_date) VALUES
(1,1,'INSPECTION','볼트 조임과 안전바 상태 점검 완료',DATE_SUB(@now,INTERVAL 20 DAY),1,DATE_ADD(@today,INTERVAL 10 DAY)),
(2,4,'INSPECTION','주행 중 소음 확인, 벨트 추가 점검 필요',DATE_SUB(@now,INTERVAL 2 DAY),1,DATE_ADD(@today,INTERVAL 1 DAY)),
(3,5,'CLEANING','레일과 시트 청소 및 윤활 완료',DATE_SUB(@now,INTERVAL 5 DAY),2,DATE_ADD(@today,INTERVAL 9 DAY)),
(4,6,'REPAIR','상단 케이블 마모로 부품 교체 요청',DATE_SUB(@now,INTERVAL 1 DAY),1,DATE_ADD(@today,INTERVAL 3 DAY)),
(5,7,'INSPECTION','측정 정확도와 프린터 상태 정상',DATE_SUB(@now,INTERVAL 15 DAY),3,DATE_ADD(@today,INTERVAL 15 DAY)),
(6,8,'REPAIR','페달 고정부 파손 확인, 사용 중지',DATE_SUB(@now,INTERVAL 7 DAY),1,DATE_ADD(@today,INTERVAL 5 DAY));

INSERT INTO notifications (id,user_id,notification_key,type,title,message,target_url,read_at,created_at) VALUES
(1,1,'demo-admin-expiring-1','MEMBERSHIP_EXPIRING','회원권 만료 예정','오세진 회원의 이용권이 5일 후 만료됩니다.','/admin/members/9',NULL,DATE_SUB(@now,INTERVAL 1 HOUR)),
(2,1,'demo-admin-reservation-1','RESERVATION','예약 확인 필요','내일 체험 PT 예약이 접수되었습니다.','/admin/reservations',NULL,DATE_SUB(@now,INTERVAL 2 HOUR)),
(3,1,'demo-admin-equipment-1','EQUIPMENT','시설 수리 진행 중','케이블 머신 부품 교체가 필요합니다.','/admin/facilities',DATE_SUB(@now,INTERVAL 30 MINUTE),DATE_SUB(@now,INTERVAL 1 DAY)),
(4,5,'demo-member-reservation-1','RESERVATION','오늘 PT 예약 안내','오늘 오전 10시에 김도윤 트레이너와 PT 예약이 있습니다.','/member/reservations',NULL,DATE_SUB(@now,INTERVAL 3 HOUR)),
(5,5,'demo-member-active-1','MEMBERSHIP','회원권 이용 안내','3개월 자유 이용권을 이용 중입니다.','/member/memberships',DATE_SUB(@now,INTERVAL 2 DAY),DATE_SUB(@now,INTERVAL 30 DAY)),
(6,6,'demo-member-expiring-1','MEMBERSHIP_EXPIRING','회원권 만료 예정','현재 이용권이 11일 후 만료됩니다.','/member/memberships',NULL,DATE_SUB(@now,INTERVAL 4 HOUR)),
(7,13,'demo-member-expiring-2','MEMBERSHIP_EXPIRING','회원권 만료 예정','현재 이용권이 5일 후 만료됩니다.','/member/memberships',NULL,DATE_SUB(@now,INTERVAL 1 HOUR));

COMMIT;

-- 실행 직후 데이터 규모 확인
SELECT 'users' table_name,COUNT(*) row_count FROM users
UNION ALL SELECT 'trainers',COUNT(*) FROM trainers UNION ALL SELECT 'members',COUNT(*) FROM members
UNION ALL SELECT 'member_memberships',COUNT(*) FROM member_memberships UNION ALL SELECT 'payments',COUNT(*) FROM payments
UNION ALL SELECT 'payment_orders',COUNT(*) FROM payment_orders UNION ALL SELECT 'reservations',COUNT(*) FROM reservations
UNION ALL SELECT 'attendances',COUNT(*) FROM attendances UNION ALL SELECT 'workout_routines',COUNT(*) FROM workout_routines
UNION ALL SELECT 'equipment',COUNT(*) FROM equipment UNION ALL SELECT 'notifications',COUNT(*) FROM notifications;
