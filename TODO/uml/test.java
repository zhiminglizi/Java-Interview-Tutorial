
=====================================
2021-06-07 23:34:29 0x7f9e65cb4700 INNODB MONITOR OUTPUT
=====================================
Per second averages calculated from the last 7 seconds
-----------------
BACKGROUND THREAD
-----------------
srv_master_thread loops: 5114424 srv_active, 0 srv_shutdown, 14722045 srv_idle
srv_master_thread log flush and writes: 19832170
----------
SEMAPHORES
----------
OS WAIT ARRAY INFO: reservation count 36041107
OS WAIT ARRAY INFO: signal count 32703412
RW-shared spins 0, rounds 11645805, OS waits 4127499
RW-excl spins 0, rounds 45779914, OS waits 950903
RW-sx spins 187503, rounds 4165206, OS waits 80783
Spin rounds per wait: 11645805.00 RW-shared, 45779914.00 RW-excl, 22.21 RW-sx
------------------------
LATEST DETECTED DEADLOCK
------------------------
2021-06-01 22:00:51 0x7f9e9f6db700
*** (1) TRANSACTION:
TRANSACTION 4385633832, ACTIVE 0 sec updating or deleting
mysql tables in use 1, locked 1
LOCK WAIT 6 lock struct(s), heap size 1136, 6 row lock(s), undo log entries 2
MySQL thread id 20231571, OS thread handle 140318965561088, query id 3402074420 10.100.249.217 dev update
REPLACE  into sub_order_index (member_level, crs_no, sub_crs_no,
      arrival_date, departure_date, profile_status,
      multiple_value, multiple_type, hotel_id,
      member_id, is_self_checkin, deleted,
      create_time, create_user, modify_time,
      modify_user, resv_profile_no)
    values
        
        ('NON', '200001421060120412988600PX04PLZD', 'S2000014210601204129957001X043XMV', '2021-06-01 20:01:20.0', '2021-06-01 20:09:33.0', 'HOTEL_ROOM_CHECK_OUT', '8C064A283FF2A94E6DEF4B74B0BE7D72', '9', '2000014', '', 0, 0, '2021-06-01 22:00:51.331', 'SyncPms', '2021-06-01 22:00:51.331', 'SyncPms', 'T2000014055281679001')
*** (1) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 19517 page no 28 n bits 312 index idx_multiple_value_multiple_type_resv_profile_no of table `orderdb_26`.`sub_order_index` trx id 4385633832 lock_mode X locks gap before rec insert intention waiting
Record lock, heap no 177 PHYSICAL RECORD: n_fields 4; compact format; info bits 32
 0: len 30; hex 384330363441323833464632413934453644454634423734423042453744; asc 8C064A283FF2A94E6DEF4B74B0BE7D; (total 32 bytes);
 1: len 1; hex 39; asc 9;;
 2: len 20; hex 5432303030303134303535323831363739303031; asc T2000014055281679001;;
 3: len 4; hex 80010a72; asc    r;;

*** (2) TRANSACTION:
TRANSACTION 4385633839, ACTIVE 0 sec inserting
mysql tables in use 1, locked 1
4 lock struct(s), heap size 1136, 2 row lock(s), undo log entries 1
MySQL thread id 20231673, OS thread handle 140319256327936, query id 3402074427 10.100.249.217 dev update
REPLACE  into sub_order_index (member_level, crs_no, sub_crs_no,
      arrival_date, departure_date, profile_status,
      multiple_value, multiple_type, hotel_id,
      member_id, is_self_checkin, deleted,
      create_time, create_user, modify_time,
      modify_user, resv_profile_no)
    values
        
        ('NON', '200001421060120412988600PX04PLZD', 'S2000014210601204129957001X043XMV', '2021-06-01 20:01:20.0', '2021-06-01 20:09:33.0', 'HOTEL_ROOM_CHECK_OUT', '8C064A283FF2A94E6DEF4B74B0BE7D72', '9', '2000014', '', 0, 0, '2021-06-01 22:00:51.363', 'SyncPms', '2021-06-01 22:00:51.363', 'SyncPms', 'T2000014055281679001')
*** (2) HOLDS THE LOCK(S):
RECORD LOCKS space id 19517 page no 28 n bits 312 index idx_multiple_value_multiple_type_resv_profile_no of table `orderdb_26`.`sub_order_index` trx id 4385633839 lock_mode X locks gap before rec
Record lock, heap no 177 PHYSICAL RECORD: n_fields 4; compact format; info bits 32
 0: len 30; hex 384330363441323833464632413934453644454634423734423042453744; asc 8C064A283FF2A94E6DEF4B74B0BE7D; (total 32 bytes);
 1: len 1; hex 39; asc 9;;
 2: len 20; hex 5432303030303134303535323831363739303031; asc T2000014055281679001;;
 3: len 4; hex 80010a72; asc    r;;

*** (2) WAITING FOR THIS LOCK TO BE GRANTED:
RECORD LOCKS space id 19517 page no 28 n bits 312 index idx_multiple_value_multiple_type_resv_profile_no of table `orderdb_26`.`sub_order_index` trx id 4385633839 lock_mode X waiting
Record lock, heap no 177 PHYSICAL RECORD: n_fields 4; compact format; info bits 32
 0: len 30; hex 384330363441323833464632413934453644454634423734423042453744; asc 8C064A283FF2A94E6DEF4B74B0BE7D; (total 32 bytes);
 1: len 1; hex 39; asc 9;;
 2: len 20; hex 5432303030303134303535323831363739303031; asc T2000014055281679001;;
 3: len 4; hex 80010a72; asc    r;;

*** WE ROLL BACK TRANSACTION (2)
------------
TRANSACTIONS
------------
Trx id counter 4464613238
Purge done for trx's n:o < 4464613238 undo n:o < 0 state: running but idle
History list length 2
LIST OF TRANSACTIONS FOR EACH SESSION:
---TRANSACTION 421800976044544, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976043632, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976045456, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976049104, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976048192, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976047280, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976046368, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976042720, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976041808, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976052752, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976079200, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976076464, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976075552, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976073728, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976185904, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
---TRANSACTION 421800976050016, not started
0 lock struct(s), heap size 1136, 0 row lock(s)
--------
FILE I/O
--------
I/O thread 0 state: waiting for completed aio requests (insert buffer thread)
I/O thread 1 state: waiting for completed aio requests (log thread)
I/O thread 2 state: waiting for completed aio requests (read thread)
I/O thread 3 state: waiting for completed aio requests (read thread)
I/O thread 4 state: waiting for completed aio requests (read thread)
I/O thread 5 state: waiting for completed aio requests (read thread)
I/O thread 6 state: waiting for completed aio requests (write thread)
I/O thread 7 state: waiting for completed aio requests (write thread)
I/O thread 8 state: waiting for completed aio requests (write thread)
I/O thread 9 state: waiting for completed aio requests (write thread)
Pending normal aio reads: [0, 0, 0, 0] , aio writes: [0, 0, 0, 0] ,
 ibuf aio reads:, log i/o's:, sync i/o's:
Pending flushes (fsync) log: 0; buffer pool: 0
5505530 OS file reads, 951355475 OS file writes, 818298558 OS fsyncs
0.00 reads/s, 0 avg bytes/read, 0.29 writes/s, 0.29 fsyncs/s
-------------------------------------
INSERT BUFFER AND ADAPTIVE HASH INDEX
-------------------------------------
Ibuf: size 1, free list len 6642, seg size 6644, 83407 merges
merged operations:
 insert 84507, delete mark 177929, delete 20750
discarded operations:
 insert 0, delete mark 0, delete 0
Hash table size 553193, node heap has 37 buffer(s)
Hash table size 553193, node heap has 106 buffer(s)
Hash table size 553193, node heap has 150 buffer(s)
Hash table size 553193, node heap has 141 buffer(s)
Hash table size 553193, node heap has 98 buffer(s)
Hash table size 553193, node heap has 415 buffer(s)
Hash table size 553193, node heap has 503 buffer(s)
Hash table size 553193, node heap has 39 buffer(s)
0.00 hash searches/s, 0.00 non-hash searches/s
---
LOG
---
Log sequence number 1213523098520
Log flushed up to   1213523098520
Pages flushed up to 1213523098520
Last checkpoint at  1213523098511
0 pending log flushes, 0 pending chkp writes
755158342 log i/o's done, 0.29 log i/o's/second
----------------------
BUFFER POOL AND MEMORY
----------------------
Total large memory allocated 2198863872
Dictionary memory allocated 6066331
Buffer pool size   131056
Free buffers       8201
Database pages     121366
Old database pages 44638
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 10467337, not young 105475546
0.00 youngs/s, 0.00 non-youngs/s
Pages read 5502201, created 761687, written 168169615
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
Buffer pool hit rate 1000 / 1000, young-making rate 0 / 1000 not 0 / 1000
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 121366, unzip_LRU len: 0
I/O sum[5944]:cur[0], unzip sum[0]:cur[0]
----------------------
INDIVIDUAL BUFFER POOL INFO
----------------------
---BUFFER POOL 0
Buffer pool size   16382
Free buffers       1025
Database pages     15171
Old database pages 5580
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1172041, not young 12676848
0.00 youngs/s, 0.00 non-youngs/s
Pages read 667343, created 92295, written 21898861
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15171, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 1
Buffer pool size   16382
Free buffers       1025
Database pages     15163
Old database pages 5577
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1431640, not young 13962711
0.00 youngs/s, 0.00 non-youngs/s
Pages read 736157, created 95590, written 8880005
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15163, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 2
Buffer pool size   16382
Free buffers       1025
Database pages     15171
Old database pages 5580
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1349112, not young 13505819
0.00 youngs/s, 0.00 non-youngs/s
Pages read 690852, created 95765, written 11865138
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15171, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 3
Buffer pool size   16382
Free buffers       1025
Database pages     15174
Old database pages 5581
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1138937, not young 11821041
0.00 youngs/s, 0.00 non-youngs/s
Pages read 624793, created 94616, written 37067605
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15174, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 4
Buffer pool size   16382
Free buffers       1025
Database pages     15176
Old database pages 5582
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1434122, not young 14038054
0.00 youngs/s, 0.00 non-youngs/s
Pages read 729050, created 96300, written 29291343
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15176, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 5
Buffer pool size   16382
Free buffers       1025
Database pages     15162
Old database pages 5576
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1327458, not young 13579773
0.00 youngs/s, 0.00 non-youngs/s
Pages read 702086, created 95588, written 18332748
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15162, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 6
Buffer pool size   16382
Free buffers       1025
Database pages     15185
Old database pages 5585
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1349030, not young 13303436
0.00 youngs/s, 0.00 non-youngs/s
Pages read 673951, created 95080, written 17220572
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
No buffer pool page gets since the last printout
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15185, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
---BUFFER POOL 7
Buffer pool size   16382
Free buffers       1026
Database pages     15164
Old database pages 5577
Modified db pages  0
Pending reads      0
Pending writes: LRU 0, flush list 0, single page 0
Pages made young 1264997, not young 12587864
0.00 youngs/s, 0.00 non-youngs/s
Pages read 677969, created 96453, written 23613343
0.00 reads/s, 0.00 creates/s, 0.00 writes/s
Buffer pool hit rate 1000 / 1000, young-making rate 0 / 1000 not 0 / 1000
Pages read ahead 0.00/s, evicted without access 0.00/s, Random read ahead 0.00/s
LRU len: 15164, unzip_LRU len: 0
I/O sum[743]:cur[0], unzip sum[0]:cur[0]
--------------
ROW OPERATIONS
--------------
0 queries inside InnoDB, 0 queries in queue
0 read views open inside InnoDB
Process ID=26515, Main thread ID=140323700393728, state: sleeping
Number of rows inserted 33434277, updated 739038616, deleted 695767, read 6545664881069
0.00 inserts/s, 0.00 updates/s, 0.00 deletes/s, 142.12 reads/s
----------------------------
END OF INNODB MONITOR OUTPUT
============================
