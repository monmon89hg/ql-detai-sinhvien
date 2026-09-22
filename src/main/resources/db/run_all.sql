-- File chay toan bo schema theo thu tu phu thuoc.
-- Hay doi qldt thanh ten database MySQL cua ban neu ten khac.

CREATE DATABASE IF NOT EXISTS qldt
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE quan_ly_sinh_vien;

-- Luu y: V2 va V3 can cac bang nen nguoi_dung, bo_mon (V1).
-- Hay chay V1 truoc khi chay file nay.

SOURCE V2_create_topics.sql;
SOURCE V3 _create_registration_tables.sql;
SOURCE V4_create_tables.sql;
