-- PostgreSQL 샘플 주문 데이터 100건
-- 목적: 배치 정산 테스트용(order 중심)
-- 기준일: 2026-03-09

BEGIN;

-- 같은 스크립트를 여러 번 실행해도 중복 없이 재생성되도록 정리
DELETE FROM public."order"
WHERE order_no LIKE 'ORD-20260309-%';

WITH src AS (
    SELECT
        gs AS n,
        ('ORD-20260309-' || LPAD(gs::text, 4, '0')) AS order_no,
        ('00000000-0000-0000-0000-' || LPAD(gs::text, 12, '0'))::uuid AS id,
        ('10000000-0000-0000-0000-' || LPAD(gs::text, 12, '0'))::uuid AS buyer_id,
        ('20000000-0000-0000-0000-' || LPAD((((gs - 1) % 5) + 1)::text, 12, '0'))::uuid AS seller_id,
        ('30000000-0000-0000-0000-' || LPAD(gs::text, 12, '0'))::uuid AS product_id,
        ((gs % 3) + 1) AS quantity,
        (1000 + (gs * 100))::numeric(15, 2) AS gross_amount,
        ROUND(((1000 + (gs * 100)) * 0.05)::numeric, 2)::numeric(15, 2) AS fee_amount,
        (CASE WHEN gs % 7 = 0 THEN 100 ELSE 0 END)::numeric(15, 2) AS refund_amount,
        CASE WHEN gs % 10 = 0 THEN 'CANCELED' ELSE 'PAID' END AS status,
        (timestamp '2026-03-09 00:00:00' + ((gs - 1) * interval '10 minute')) AS paid_at,
        CASE WHEN gs % 15 = 0 THEN true ELSE false END AS settled
    FROM generate_series(1, 100) gs
)
INSERT INTO public."order" (
    id,
    order_no,
    buyer_id,
    seller_id,
    product_id,
    quantity,
    gross_amount,
    fee_amount,
    refund_amount,
    net_amount,
    status,
    paid_at,
    settled,
    settlement_batch_id,
    reg_id,
    reg_dt,
    modify_id,
    modify_dt
)
SELECT
    id,
    order_no,
    buyer_id,
    seller_id,
    product_id,
    quantity,
    gross_amount,
    fee_amount,
    refund_amount,
    (gross_amount - fee_amount - refund_amount)::numeric(15, 2) AS net_amount,
    status,
    paid_at,
    settled,
    CASE WHEN settled THEN '77777777-7777-7777-7777-777777777777'::uuid ELSE NULL END AS settlement_batch_id,
    buyer_id AS reg_id,
    now() AS reg_dt,
    buyer_id AS modify_id,
    now() AS modify_dt
FROM src;

COMMIT;

-- 검증용 요약
-- 전체 100건, 그중 정산 후보(status=PAID and settled=false)는 87건
SELECT count(*) AS total_count FROM public."order" WHERE order_no LIKE 'ORD-20260309-%';
SELECT count(*) AS settlement_candidate_count
FROM public."order"
WHERE order_no LIKE 'ORD-20260309-%'
  AND status = 'PAID'
  AND settled = false;
