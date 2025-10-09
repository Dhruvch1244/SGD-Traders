
INSERT INTO PORTFOLIOS (clientid) VALUES ('C001');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C002');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C003');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C004');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C005');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C006');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C007');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C008');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C009');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C010');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C011');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C012');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C013');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C014');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C015');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C016');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C017');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C018');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C019');
INSERT INTO PORTFOLIOS (clientid) VALUES ('C020');

-- Next, populate the PORTFOLIO_HOLDINGS table with the final calculated positions
-- Holdings for Client C001
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN012', 100);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN078', 25000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN045', 50000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN005', 70);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN033', 25);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN003', 30);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C001', 'IN047', 50000);

-- Holdings for Client C002
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C002', 'IN021', 30);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C002', 'IN088', 10000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C002', 'IN051', 75000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C002', 'IN015', 200);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C002', 'IN006', 10);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C002', 'IN050', 60000);

-- Holdings for Client C003
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C003', 'IN038', 400);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C003', 'IN095', 5000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C003', 'IN062', 20000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C003', 'IN009', 10);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C003', 'IN052', 70000);

-- Holdings for Client C004
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C004', 'IN002', 100);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C004', 'IN071', 150000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C004', 'IN029', 300);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C004', 'IN010', 25);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C004', 'IN054', 80000);

-- Holdings for Client C005
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C005', 'IN019', 150);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C005', 'IN081', 40000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C005', 'IN058', 80000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C005', 'IN013', 20);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C005', 'IN056', 90000);

-- Holdings for Client C006
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C006', 'IN031', 45);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C006', 'IN091', 15000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C006', 'IN048', 60000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C006', 'IN016', 25);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C006', 'IN057', 100000);

-- Holdings for Client C007
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C007', 'IN008', 40);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C007', 'IN065', 120000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C007', 'IN017', 20);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C007', 'IN060', 110000);

-- Holdings for Client C008
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C008', 'IN025', 85);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C008', 'IN085', 75000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C008', 'IN053', 30000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C008', 'IN020', 30);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C008', 'IN061', 120000);

-- Holdings for Client C009
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C009', 'IN035', 300);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C009', 'IN099', 20000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C009', 'IN069', 100000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C009', 'IN023', 50);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C009', 'IN064', 130000);

-- Holdings for Client C010
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C010', 'IN011', 70);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C010', 'IN076', 60000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C010', 'IN026', 200);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C010', 'IN066', 140000);

-- Holdings for Client C011
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C011', 'IN042', 200000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C011', 'IN001', 50);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C011', 'IN027', 10);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C011', 'IN067', 150000);

-- Holdings for Client C012
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C012', 'IN022', 450);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C012', 'IN089', 35000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C012', 'IN030', 70);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C012', 'IN068', 160000);

-- Holdings for Client C013
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C013', 'IN055', 90000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C013', 'IN014', 200);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C013', 'IN034', 40);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C013', 'IN070', 170000);

-- Holdings for Client C014
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C014', 'IN037', 100);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C014', 'IN096', 45000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C014', 'IN036', 80);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C014', 'IN072', 180000);

-- Holdings for Client C015
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C015', 'IN063', 110000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C015', 'IN007', 100);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C015', 'IN039', 50);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C015', 'IN073', 190000);

-- Holdings for Client C016
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C016', 'IN028', 150);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C016', 'IN082', 80000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C016', 'IN040', 300);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C016', 'IN074', 200000);

-- Holdings for Client C017
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C017', 'IN049', 70000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C017', 'IN018', 140);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C017', 'IN041', 10000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C017', 'IN075', 210000);

-- Holdings for Client C018
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C018', 'IN032', 250);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C018', 'IN092', 55000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C018', 'IN043', 20000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C018', 'IN077', 220000);

-- Holdings for Client C019
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C019', 'IN059', 130000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C019', 'IN004', 75);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C019', 'IN044', 30000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C019', 'IN079', 230000);
-- Holdings for Client C020
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C020', 'IN024', 120);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C020', 'IN086', 65000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C020', 'IN046', 40000);
INSERT INTO PORTFOLIO_HOLDINGS (portfolioid, instrumentid, quantity) VALUES ('C020', 'IN080', 240000);