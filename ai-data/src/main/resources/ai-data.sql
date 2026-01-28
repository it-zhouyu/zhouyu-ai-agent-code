-- MySQL dump 10.13  Distrib 8.0.34, for macos13 (arm64)
--
-- Host: 127.0.0.1    Database: zhouyu_db
-- ------------------------------------------------------
-- Server version	5.7.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `address_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省',
  `city` varchar(50) NOT NULL COMMENT '市',
  `district` varchar(50) NOT NULL COMMENT '区',
  `detail_address` varchar(200) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT '0' COMMENT '是否默认地址(0:否 1:是)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,1,'张三','13800138001','北京市','北京市','朝阳区','建国路100号',1,'2024-01-01 10:00:00','2024-01-01 10:00:00'),(2,1,'张三','13800138001','上海市','上海市','浦东新区','陆家嘴金融中心',0,'2024-01-02 10:00:00','2024-01-02 10:00:00'),(3,2,'李四','13800138002','广东省','深圳市','南山区','科技园南区',1,'2024-01-02 11:00:00','2024-01-02 11:00:00'),(4,3,'王五','13800138003','浙江省','杭州市','西湖区','文三路200号',1,'2024-01-03 12:00:00','2024-01-03 12:00:00'),(5,4,'赵六','13800138004','江苏省','南京市','鼓楼区','中山北路100号',1,'2024-01-04 13:00:00','2024-01-04 13:00:00'),(6,5,'孙七','13800138005','四川省','成都市','武侯区','天府软件园',1,'2024-01-05 14:00:00','2024-01-05 14:00:00'),(7,6,'周八','13800138006','湖北省','武汉市','洪山区','光谷广场',1,'2024-01-06 15:00:00','2024-01-06 15:00:00'),(8,7,'吴九','13800138007','陕西省','西安市','雁塔区','高新路50号',1,'2024-01-07 16:00:00','2024-01-07 16:00:00'),(9,8,'郑十','13800138008','湖南省','长沙市','岳麓区','岳麓大道',1,'2024-01-08 17:00:00','2024-01-08 17:00:00'),(10,9,'钱一','13800138009','福建省','厦门市','思明区','软件园二期',1,'2024-01-09 18:00:00','2024-01-09 18:00:00'),(11,10,'孙二','13800138010','山东省','青岛市','市南区','香港中路',1,'2024-01-10 19:00:00','2024-01-10 19:00:00'),(12,11,'李三','13800138011','辽宁省','大连市','中山区','人民路',1,'2024-01-11 20:00:00','2024-01-11 20:00:00'),(13,12,'王四','13800138012','重庆市','重庆市','渝中区','解放碑',1,'2024-01-12 21:00:00','2024-01-12 21:00:00'),(14,13,'赵五','13800138013','天津市','天津市','和平区','南京路',1,'2024-01-13 22:00:00','2024-01-13 22:00:00'),(15,14,'钱六','13800138014','河南省','郑州市','金水区','农业路',1,'2024-01-14 23:00:00','2024-01-14 23:00:00'),(16,15,'孙七','13800138015','河北省','石家庄市','长安区','中山东路',1,'2024-01-15 09:00:00','2024-01-15 09:00:00'),(17,16,'李八','13800138016','吉林省','长春市','朝阳区','人民大街',1,'2024-01-16 10:00:00','2024-01-16 10:00:00'),(18,17,'王九','13800138017','黑龙江省','哈尔滨市','道里区','中央大街',1,'2024-01-17 11:00:00','2024-01-17 11:00:00'),(19,18,'周十','13800138018','安徽省','合肥市','包河区','徽州大道',1,'2024-01-18 12:00:00','2024-01-18 12:00:00'),(20,19,'吴十','13800138019','江西省','南昌市','东湖区','阳明路',1,'2024-01-19 13:00:00','2024-01-19 13:00:00');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brand`
--

DROP TABLE IF EXISTS `brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brand` (
  `brand_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '品牌ID',
  `brand_name` varchar(50) NOT NULL COMMENT '品牌名称',
  `brand_logo` varchar(255) DEFAULT NULL COMMENT '品牌logo',
  `description` text COMMENT '品牌描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`brand_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brand`
--

LOCK TABLES `brand` WRITE;
/*!40000 ALTER TABLE `brand` DISABLE KEYS */;
INSERT INTO `brand` VALUES (1,'Apple','apple_logo.png','美国科技公司',1,'2024-01-01 10:00:00','2024-01-01 10:00:00'),(2,'华为','huawei_logo.png','中国科技公司',1,'2024-01-01 11:00:00','2024-01-01 11:00:00'),(3,'小米','xiaomi_logo.png','中国科技公司',1,'2024-01-01 12:00:00','2024-01-01 12:00:00'),(4,'联想','lenovo_logo.png','中国电脑制造商',1,'2024-01-01 13:00:00','2024-01-01 13:00:00'),(5,'戴尔','dell_logo.png','美国电脑制造商',1,'2024-01-01 14:00:00','2024-01-01 14:00:00'),(6,'索尼','sony_logo.png','日本电子公司',1,'2024-01-01 15:00:00','2024-01-01 15:00:00'),(7,'任天堂','nintendo_logo.png','日本游戏公司',1,'2024-01-01 16:00:00','2024-01-01 16:00:00'),(8,'佳能','canon_logo.png','日本相机制造商',1,'2024-01-01 17:00:00','2024-01-01 17:00:00'),(9,'大疆','dji_logo.png','中国无人机制造商',1,'2024-01-01 18:00:00','2024-01-01 18:00:00'),(10,'三星','samsung_logo.png','韩国电子公司',1,'2024-01-01 19:00:00','2024-01-01 19:00:00'),(11,'罗技','logitech_logo.png','瑞士外设制造商',1,'2024-01-01 20:00:00','2024-01-01 20:00:00'),(12,'明基','benq_logo.png','台湾显示设备制造商',1,'2024-01-01 21:00:00','2024-01-01 21:00:00'),(13,'飞利浦','philips_logo.png','荷兰电子公司',1,'2024-01-01 22:00:00','2024-01-01 22:00:00'),(14,'海信','hisense_logo.png','中国电子公司',1,'2024-01-01 23:00:00','2024-01-01 23:00:00'),(15,'TCL','tcl_logo.png','中国电子公司',1,'2024-01-02 09:00:00','2024-01-02 09:00:00'),(16,'创维','skyworth_logo.png','中国电子公司',1,'2024-01-02 10:00:00','2024-01-02 10:00:00'),(17,'格力','gree_logo.png','中国空调制造商',1,'2024-01-02 11:00:00','2024-01-02 11:00:00'),(18,'美的','midea_logo.png','中国家电制造商',1,'2024-01-02 12:00:00','2024-01-02 12:00:00'),(19,'海尔','haier_logo.png','中国家电制造商',1,'2024-01-02 13:00:00','2024-01-02 13:00:00'),(20,'苏泊尔','supor_logo.png','中国厨具制造商',1,'2024-01-02 14:00:00','2024-01-02 14:00:00');
/*!40000 ALTER TABLE `brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `cart_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `quantity` int(11) NOT NULL COMMENT '商品数量',
  `selected` tinyint(1) DEFAULT '1' COMMENT '是否选中(0:否 1:是)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (1,1,2,1,1,'2024-01-20 10:00:00','2024-01-20 10:00:00'),(2,1,6,2,1,'2024-01-20 10:05:00','2024-01-20 10:05:00'),(3,2,3,1,1,'2024-01-20 11:00:00','2024-01-20 11:00:00'),(4,2,7,1,0,'2024-01-20 11:05:00','2024-01-20 11:05:00'),(5,3,4,1,1,'2024-01-20 12:00:00','2024-01-20 12:00:00'),(6,3,8,1,1,'2024-01-20 12:05:00','2024-01-20 12:05:00'),(7,4,5,1,1,'2024-01-20 13:00:00','2024-01-20 13:00:00'),(8,4,9,1,0,'2024-01-20 13:05:00','2024-01-20 13:05:00'),(9,5,10,1,1,'2024-01-20 14:00:00','2024-01-20 14:00:00'),(10,5,11,2,1,'2024-01-20 14:05:00','2024-01-20 14:05:00'),(11,6,12,1,1,'2024-01-20 15:00:00','2024-01-20 15:00:00'),(12,6,13,1,1,'2024-01-20 15:05:00','2024-01-20 15:05:00'),(13,7,14,1,1,'2024-01-20 16:00:00','2024-01-20 16:00:00'),(14,7,15,1,0,'2024-01-20 16:05:00','2024-01-20 16:05:00'),(15,8,16,1,1,'2024-01-20 17:00:00','2024-01-20 17:00:00'),(16,8,17,1,1,'2024-01-20 17:05:00','2024-01-20 17:05:00'),(17,9,18,1,1,'2024-01-20 18:00:00','2024-01-20 18:00:00'),(18,9,19,2,1,'2024-01-20 18:05:00','2024-01-20 18:05:00'),(19,10,20,1,1,'2024-01-20 19:00:00','2024-01-20 19:00:00'),(20,10,1,1,0,'2024-01-20 19:05:00','2024-01-20 19:05:00');
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `category_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint(20) DEFAULT '0' COMMENT '父级分类ID',
  `level` int(11) DEFAULT '1' COMMENT '分类层级',
  `sort` int(11) DEFAULT '0' COMMENT '排序',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'手机通讯',0,1,1,1,'2024-01-01 10:00:00','2024-01-01 10:00:00'),(2,'电脑办公',0,1,2,1,'2024-01-01 11:00:00','2024-01-01 11:00:00'),(3,'影音娱乐',0,1,3,1,'2024-01-01 12:00:00','2024-01-01 12:00:00'),(4,'智能设备',0,1,4,1,'2024-01-01 13:00:00','2024-01-01 13:00:00'),(5,'摄影摄像',0,1,5,1,'2024-01-01 14:00:00','2024-01-01 14:00:00'),(6,'智能家居',0,1,6,1,'2024-01-01 15:00:00','2024-01-01 15:00:00'),(7,'个护健康',0,1,7,1,'2024-01-01 16:00:00','2024-01-01 16:00:00'),(8,'厨卫电器',0,1,8,1,'2024-01-01 17:00:00','2024-01-01 17:00:00'),(9,'生活电器',0,1,9,1,'2024-01-01 18:00:00','2024-01-01 18:00:00'),(10,'大家电',0,1,10,1,'2024-01-01 19:00:00','2024-01-01 19:00:00'),(11,'手机配件',1,2,1,1,'2024-01-02 10:00:00','2024-01-02 10:00:00'),(12,'笔记本电脑',2,2,1,1,'2024-01-02 11:00:00','2024-01-02 11:00:00'),(13,'台式电脑',2,2,2,1,'2024-01-02 12:00:00','2024-01-02 12:00:00'),(14,'耳机耳麦',3,2,1,1,'2024-01-02 13:00:00','2024-01-02 13:00:00'),(15,'音箱音响',3,2,2,1,'2024-01-02 14:00:00','2024-01-02 14:00:00'),(16,'智能手表',4,2,1,1,'2024-01-02 15:00:00','2024-01-02 15:00:00'),(17,'智能手环',4,2,2,1,'2024-01-02 16:00:00','2024-01-02 16:00:00'),(18,'数码相机',5,2,1,1,'2024-01-02 17:00:00','2024-01-02 17:00:00'),(19,'摄像机',5,2,2,1,'2024-01-02 18:00:00','2024-01-02 18:00:00'),(20,'智能门锁',6,2,1,1,'2024-01-02 19:00:00','2024-01-02 19:00:00');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `coupon_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `coupon_name` varchar(100) NOT NULL COMMENT '优惠券名称',
  `coupon_type` tinyint(1) DEFAULT '1' COMMENT '优惠券类型(1:满减券 2:折扣券)',
  `threshold_amount` decimal(10,2) DEFAULT NULL COMMENT '使用门槛金额',
  `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '优惠金额',
  `discount_rate` decimal(5,2) DEFAULT NULL COMMENT '折扣比例',
  `validity_period` int(11) DEFAULT NULL COMMENT '有效天数',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (1,'新用户满减券',1,100.00,10.00,NULL,30,1,'2024-01-01 10:00:00','2024-01-01 10:00:00'),(2,'周年庆折扣券',2,200.00,NULL,0.90,15,1,'2024-01-01 11:00:00','2024-01-01 11:00:00'),(3,'满500减50',1,500.00,50.00,NULL,60,1,'2024-01-01 12:00:00','2024-01-01 12:00:00'),(4,'会员专属券',2,1000.00,NULL,0.85,90,1,'2024-01-01 13:00:00','2024-01-01 13:00:00'),(5,'节假日满减',1,300.00,30.00,NULL,7,1,'2024-01-01 14:00:00','2024-01-01 14:00:00'),(6,'清仓折扣券',2,150.00,NULL,0.80,30,1,'2024-01-01 15:00:00','2024-01-01 15:00:00'),(7,'满1000减100',1,1000.00,100.00,NULL,45,1,'2024-01-01 16:00:00','2024-01-01 16:00:00'),(8,'新品体验券',2,500.00,NULL,0.95,14,1,'2024-01-01 17:00:00','2024-01-01 17:00:00'),(9,'满2000减200',1,2000.00,200.00,NULL,90,1,'2024-01-01 18:00:00','2024-01-01 18:00:00'),(10,'品牌专属券',2,800.00,NULL,0.88,60,1,'2024-01-01 19:00:00','2024-01-01 19:00:00'),(11,'满150减20',1,150.00,20.00,NULL,30,1,'2024-01-01 20:00:00','2024-01-01 20:00:00'),(12,'限时折扣',2,100.00,NULL,0.92,3,1,'2024-01-01 21:00:00','2024-01-01 21:00:00'),(13,'满800减80',1,800.00,80.00,NULL,30,1,'2024-01-01 22:00:00','2024-01-01 22:00:00'),(14,'生日专属券',2,0.00,NULL,0.75,365,1,'2024-01-01 23:00:00','2024-01-01 23:00:00'),(15,'满3000减300',1,3000.00,300.00,NULL,60,1,'2024-01-02 09:00:00','2024-01-02 09:00:00'),(16,'VIP专属券',2,2000.00,NULL,0.70,180,1,'2024-01-02 10:00:00','2024-01-02 10:00:00'),(17,'满100减15',1,100.00,15.00,NULL,30,1,'2024-01-02 11:00:00','2024-01-02 11:00:00'),(18,'周末特惠券',2,50.00,NULL,0.85,2,1,'2024-01-02 12:00:00','2024-01-02 12:00:00'),(19,'满5000减500',1,5000.00,500.00,NULL,90,1,'2024-01-02 13:00:00','2024-01-02 13:00:00'),(20,'年终大促券',2,0.00,NULL,0.60,10,1,'2024-01-02 14:00:00','2024-01-02 14:00:00');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `order_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `pay_type` tinyint(1) DEFAULT NULL COMMENT '支付方式(1:支付宝 2:微信 3:银行卡)',
  `status` tinyint(1) DEFAULT '0' COMMENT '订单状态(0:待支付 1:已支付 2:已发货 3:已完成 4:已取消)',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'ORDER202401010001',1,8999.00,8999.00,1,3,'2024-01-01 11:00:00','2024-01-02 10:00:00','2024-01-04 15:00:00','2024-01-01 10:30:00','2024-01-04 15:00:00'),(2,'ORDER202401020001',2,13998.00,12998.00,2,3,'2024-01-02 12:00:00','2024-01-03 09:00:00','2024-01-05 14:00:00','2024-01-02 11:30:00','2024-01-05 14:00:00'),(3,'ORDER202401030001',3,5999.00,5999.00,1,2,'2024-01-03 13:00:00','2024-01-04 10:00:00',NULL,'2024-01-03 12:30:00','2024-01-04 10:00:00'),(4,'ORDER202401040001',4,18999.00,17999.00,3,1,'2024-01-04 14:00:00',NULL,NULL,'2024-01-04 13:30:00','2024-01-04 14:00:00'),(5,'ORDER202401050001',5,4399.00,4399.00,2,0,NULL,NULL,NULL,'2024-01-05 14:30:00','2024-01-05 14:30:00'),(6,'ORDER202401060001',6,2299.00,2299.00,1,3,'2024-01-06 16:00:00','2024-01-07 11:00:00','2024-01-09 16:00:00','2024-01-06 15:30:00','2024-01-09 16:00:00'),(7,'ORDER202401070001',7,6999.00,6999.00,2,3,'2024-01-07 17:00:00','2024-01-08 14:00:00','2024-01-10 10:00:00','2024-01-07 16:30:00','2024-01-10 10:00:00'),(8,'ORDER202401080001',8,2999.00,2999.00,1,2,'2024-01-08 18:00:00','2024-01-09 15:00:00',NULL,'2024-01-08 17:30:00','2024-01-09 15:00:00'),(9,'ORDER202401090001',9,1488.00,1488.00,3,1,'2024-01-09 19:00:00',NULL,NULL,'2024-01-09 18:30:00','2024-01-09 19:00:00'),(10,'ORDER202401100001',10,3899.00,3899.00,2,0,NULL,NULL,NULL,'2024-01-10 19:30:00','2024-01-10 19:30:00'),(11,'ORDER202401110001',11,2399.00,2399.00,1,3,'2024-01-11 20:00:00','2024-01-12 16:00:00','2024-01-14 11:00:00','2024-01-11 20:30:00','2024-01-14 11:00:00'),(12,'ORDER202401120001',12,25999.00,24999.00,2,3,'2024-01-12 21:00:00','2024-01-13 17:00:00','2024-01-15 14:00:00','2024-01-12 21:30:00','2024-01-15 14:00:00'),(13,'ORDER202401130001',13,6988.00,6988.00,1,2,'2024-01-13 22:00:00','2024-01-14 18:00:00',NULL,'2024-01-13 22:30:00','2024-01-14 18:00:00'),(14,'ORDER202401140001',14,699.00,699.00,3,1,'2024-01-14 23:00:00',NULL,NULL,'2024-01-14 23:30:00','2024-01-14 23:00:00'),(15,'ORDER202401150001',15,599.00,599.00,2,0,NULL,NULL,NULL,'2024-01-15 09:30:00','2024-01-15 09:30:00'),(16,'ORDER202401160001',16,499.00,499.00,1,3,'2024-01-16 10:00:00','2024-01-17 09:00:00','2024-01-19 13:00:00','2024-01-16 10:30:00','2024-01-19 13:00:00'),(17,'ORDER202401170001',17,3999.00,3999.00,2,3,'2024-01-17 11:00:00','2024-01-18 10:00:00','2024-01-20 15:00:00','2024-01-17 11:30:00','2024-01-20 15:00:00'),(18,'ORDER202401180001',18,1499.00,1499.00,1,2,'2024-01-18 12:00:00','2024-01-19 11:00:00',NULL,'2024-01-18 12:30:00','2024-01-19 11:00:00'),(19,'ORDER202401190001',19,1899.00,1899.00,3,1,'2024-01-19 13:00:00',NULL,NULL,'2024-01-19 13:30:00','2024-01-19 13:00:00'),(20,'ORDER202401200001',20,9999.00,9999.00,2,0,NULL,NULL,NULL,'2024-01-20 14:30:00','2024-01-20 14:30:00');
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `order_item_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单商品ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `quantity` int(11) NOT NULL COMMENT '购买数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '商品总金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='订单商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,1,'iPhone 15 Pro Max 256GB','iphone15.jpg',8999.00,1,8999.00,'2024-01-01 10:30:00'),(2,2,1,'iPhone 15 Pro Max 256GB','iphone15.jpg',8999.00,1,8999.00,'2024-01-02 11:30:00'),(3,2,6,'AirPods Pro 2代','airpods.jpg',1899.00,1,1899.00,'2024-01-02 11:30:00'),(4,3,3,'小米14 Ultra 512GB','mi14.jpg',5999.00,1,5999.00,'2024-01-03 12:30:00'),(5,4,4,'MacBook Pro 16寸 M3','macbook.jpg',18999.00,1,18999.00,'2024-01-04 13:30:00'),(6,5,8,'iPad Air 5代 64GB','ipad.jpg',4399.00,1,4399.00,'2024-01-05 14:30:00'),(7,6,20,'索尼WH-1000XM5耳机','sonyheadphone.jpg',2299.00,1,2299.00,'2024-01-06 15:30:00'),(8,7,2,'华为Mate 60 Pro 512GB','mate60.jpg',6999.00,1,6999.00,'2024-01-07 16:30:00'),(9,8,10,'Apple Watch Series 9','watch.jpg',2999.00,1,2999.00,'2024-01-08 17:30:00'),(10,9,11,'华为Watch GT 4','hwwatch.jpg',1488.00,1,1488.00,'2024-01-09 18:30:00'),(11,10,12,'索尼PS5游戏主机','ps5.jpg',3899.00,1,3899.00,'2024-01-10 19:30:00'),(12,11,13,'任天堂Switch OLED','switch.jpg',2399.00,1,2399.00,'2024-01-11 20:30:00'),(13,12,14,'佳能EOS R5相机','camera.jpg',25999.00,1,25999.00,'2024-01-12 21:30:00'),(14,13,15,'大疆Air 3无人机','drone.jpg',6988.00,1,6988.00,'2024-01-13 22:30:00'),(15,14,16,'三星980 Pro 1TB SSD','ssd.jpg',699.00,1,699.00,'2024-01-14 23:30:00'),(16,15,17,'罗技MX Keys键盘','keyboard.jpg',599.00,1,599.00,'2024-01-15 09:30:00'),(17,16,18,'罗技MX Anywhere 3鼠标','mouse.jpg',499.00,1,499.00,'2024-01-16 10:30:00'),(18,17,19,'明基EW3280U显示器','monitor.jpg',3999.00,1,3999.00,'2024-01-17 11:30:00'),(19,18,7,'华为FreeBuds Pro 3','freebuds.jpg',1499.00,1,1499.00,'2024-01-18 12:30:00'),(20,19,6,'AirPods Pro 2代','airpods.jpg',1899.00,1,1899.00,'2024-01-19 13:30:00');
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `payment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '支付ID',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `payment_no` varchar(50) NOT NULL COMMENT '支付流水号',
  `payment_type` tinyint(1) NOT NULL COMMENT '支付方式(1:支付宝 2:微信 3:银行卡)',
  `payment_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `payment_status` tinyint(1) DEFAULT '0' COMMENT '支付状态(0:待支付 1:支付成功 2:支付失败)',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`payment_id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='支付信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,1,'PAY202401010001',1,8999.00,1,'2024-01-01 11:00:00','2024-01-01 10:30:00','2024-01-01 11:00:00'),(2,2,'PAY202401020001',2,12998.00,1,'2024-01-02 12:00:00','2024-01-02 11:30:00','2024-01-02 12:00:00'),(3,3,'PAY202401030001',1,5999.00,1,'2024-01-03 13:00:00','2024-01-03 12:30:00','2024-01-03 13:00:00'),(4,4,'PAY202401040001',3,17999.00,1,'2024-01-04 14:00:00','2024-01-04 13:30:00','2024-01-04 14:00:00'),(5,6,'PAY202401060001',1,2299.00,1,'2024-01-06 16:00:00','2024-01-06 15:30:00','2024-01-06 16:00:00'),(6,7,'PAY202401070001',2,6999.00,1,'2024-01-07 17:00:00','2024-01-07 16:30:00','2024-01-07 17:00:00'),(7,8,'PAY202401080001',1,2999.00,1,'2024-01-08 18:00:00','2024-01-08 17:30:00','2024-01-08 18:00:00'),(8,9,'PAY202401090001',3,1488.00,1,'2024-01-09 19:00:00','2024-01-09 18:30:00','2024-01-09 19:00:00'),(9,11,'PAY202401110001',1,2399.00,1,'2024-01-11 20:00:00','2024-01-11 20:30:00','2024-01-11 20:00:00'),(10,12,'PAY202401120001',2,24999.00,1,'2024-01-12 21:00:00','2024-01-12 21:30:00','2024-01-12 21:00:00'),(11,13,'PAY202401130001',1,6988.00,1,'2024-01-13 22:00:00','2024-01-13 22:30:00','2024-01-13 22:00:00'),(12,14,'PAY202401140001',3,699.00,1,'2024-01-14 23:00:00','2024-01-14 23:30:00','2024-01-14 23:00:00'),(13,16,'PAY202401160001',1,499.00,1,'2024-01-16 10:00:00','2024-01-16 10:30:00','2024-01-16 10:00:00'),(14,17,'PAY202401170001',2,3999.00,1,'2024-01-17 11:00:00','2024-01-17 11:30:00','2024-01-17 11:00:00'),(15,18,'PAY202401180001',1,1499.00,1,'2024-01-18 12:00:00','2024-01-18 12:30:00','2024-01-18 12:00:00'),(16,19,'PAY202401190001',3,1899.00,1,'2024-01-19 13:00:00','2024-01-19 13:30:00','2024-01-19 13:00:00'),(17,5,'PAY202401050001',2,4399.00,0,NULL,'2024-01-05 14:30:00','2024-01-05 14:30:00'),(18,10,'PAY202401100001',2,3899.00,0,NULL,'2024-01-10 19:30:00','2024-01-10 19:30:00'),(19,15,'PAY202401150001',2,599.00,0,NULL,'2024-01-15 09:30:00','2024-01-15 09:30:00'),(20,20,'PAY202401200001',2,9999.00,0,NULL,'2024-01-20 14:30:00','2024-01-20 14:30:00');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌ID',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `market_price` decimal(10,2) DEFAULT NULL COMMENT '市场价',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `sales` int(11) DEFAULT '0' COMMENT '销量',
  `main_image` varchar(255) DEFAULT NULL COMMENT '主图',
  `sub_images` text COMMENT '子图列表',
  `description` text COMMENT '商品描述',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:下架 1:上架)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'iPhone 15 Pro Max 256GB',1,1,8999.00,9999.00,100,50,'iphone15.jpg','iphone15_1.jpg,iphone15_2.jpg','最新款iPhone手机',1,'2024-01-01 10:00:00','2024-01-20 10:00:00'),(2,'华为Mate 60 Pro 512GB',1,2,6999.00,7999.00,200,80,'mate60.jpg','mate60_1.jpg,mate60_2.jpg','华为旗舰手机',1,'2024-01-02 11:00:00','2024-01-20 11:00:00'),(3,'小米14 Ultra 512GB',1,3,5999.00,6499.00,150,60,'mi14.jpg','mi14_1.jpg,mi14_2.jpg','小米影像旗舰',1,'2024-01-03 12:00:00','2024-01-20 12:00:00'),(4,'MacBook Pro 16寸 M3',2,4,18999.00,19999.00,50,20,'macbook.jpg','macbook_1.jpg,macbook_2.jpg','苹果笔记本电脑',1,'2024-01-04 13:00:00','2024-01-20 13:00:00'),(5,'联想拯救者Y9000P',2,5,9999.00,10999.00,80,30,'lenovo.jpg','lenovo_1.jpg,lenovo_2.jpg','游戏笔记本电脑',1,'2024-01-05 14:00:00','2024-01-20 14:00:00'),(6,'AirPods Pro 2代',3,4,1899.00,1999.00,200,100,'airpods.jpg','airpods_1.jpg,airpods_2.jpg','无线蓝牙耳机',1,'2024-01-06 15:00:00','2024-01-20 15:00:00'),(7,'华为FreeBuds Pro 3',3,2,1499.00,1699.00,150,70,'freebuds.jpg','freebuds_1.jpg,freebuds_2.jpg','华为降噪耳机',1,'2024-01-07 16:00:00','2024-01-20 16:00:00'),(8,'iPad Air 5代 64GB',4,4,4399.00,4799.00,120,40,'ipad.jpg','ipad_1.jpg,ipad_2.jpg','苹果平板电脑',1,'2024-01-08 17:00:00','2024-01-20 17:00:00'),(9,'小米平板6 Pro',4,3,2399.00,2699.00,180,60,'mipad.jpg','mipad_1.jpg,mipad_2.jpg','小米平板电脑',1,'2024-01-09 18:00:00','2024-01-20 18:00:00'),(10,'Apple Watch Series 9',5,4,2999.00,3199.00,100,30,'watch.jpg','watch_1.jpg,watch_2.jpg','智能手表',1,'2024-01-10 19:00:00','2024-01-20 19:00:00'),(11,'华为Watch GT 4',5,2,1488.00,1688.00,150,50,'hwwatch.jpg','hwwatch_1.jpg,hwwatch_2.jpg','运动手表',1,'2024-01-11 20:00:00','2024-01-20 20:00:00'),(12,'索尼PS5游戏主机',6,6,3899.00,4299.00,60,25,'ps5.jpg','ps5_1.jpg,ps5_2.jpg','游戏机',1,'2024-01-12 21:00:00','2024-01-20 21:00:00'),(13,'任天堂Switch OLED',6,7,2399.00,2599.00,90,35,'switch.jpg','switch_1.jpg,switch_2.jpg','掌上游戏机',1,'2024-01-13 22:00:00','2024-01-20 22:00:00'),(14,'佳能EOS R5相机',7,8,25999.00,27999.00,30,10,'camera.jpg','camera_1.jpg,camera_2.jpg','全画幅微单',1,'2024-01-14 23:00:00','2024-01-20 23:00:00'),(15,'大疆Air 3无人机',8,9,6988.00,7688.00,40,15,'drone.jpg','drone_1.jpg,drone_2.jpg','航拍无人机',1,'2024-01-15 09:00:00','2024-01-20 09:00:00'),(16,'三星980 Pro 1TB SSD',9,10,699.00,799.00,300,120,'ssd.jpg','ssd_1.jpg,ssd_2.jpg','固态硬盘',1,'2024-01-16 10:00:00','2024-01-20 10:00:00'),(17,'罗技MX Keys键盘',10,11,599.00,699.00,200,80,'keyboard.jpg','keyboard_1.jpg,keyboard_2.jpg','无线键盘',1,'2024-01-17 11:00:00','2024-01-20 11:00:00'),(18,'罗技MX Anywhere 3鼠标',10,11,499.00,599.00,180,70,'mouse.jpg','mouse_1.jpg,mouse_2.jpg','无线鼠标',1,'2024-01-18 12:00:00','2024-01-20 12:00:00'),(19,'明基EW3280U显示器',11,12,3999.00,4499.00,50,20,'monitor.jpg','monitor_1.jpg,monitor_2.jpg','4K显示器',1,'2024-01-19 13:00:00','2024-01-20 13:00:00'),(20,'索尼WH-1000XM5耳机',3,6,2299.00,2599.00,120,45,'sonyheadphone.jpg','sony_1.jpg,sony_2.jpg','降噪耳机',1,'2024-01-20 14:00:00','2024-01-20 14:00:00');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `gender` tinyint(1) DEFAULT '0' COMMENT '性别(0:未知 1:男 2:女)',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint(1) DEFAULT '1' COMMENT '状态(0:禁用 1:正常)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'zhangsan','zhangsan@email.com','13800138001','encrypted_pwd_1','张三','avatar1.jpg',1,'1990-01-15',1,'2024-01-01 10:00:00','2024-01-01 10:00:00'),(2,'lisi','lisi@email.com','13800138002','encrypted_pwd_2','李四','avatar2.jpg',2,'1992-05-20',1,'2024-01-02 11:00:00','2024-01-02 11:00:00'),(3,'wangwu','wangwu@email.com','13800138003','encrypted_pwd_3','王五','avatar3.jpg',1,'1988-12-10',1,'2024-01-03 12:00:00','2024-01-03 12:00:00'),(4,'zhaoliu','zhaoliu@email.com','13800138004','encrypted_pwd_4','赵六','avatar4.jpg',2,'1995-08-25',1,'2024-01-04 13:00:00','2024-01-04 13:00:00'),(5,'sunqi','sunqi@email.com','13800138005','encrypted_pwd_5','孙七','avatar5.jpg',1,'1993-03-18',1,'2024-01-05 14:00:00','2024-01-05 14:00:00'),(6,'zhouba','zhouba@email.com','13800138006','encrypted_pwd_6','周八','avatar6.jpg',2,'1991-07-22',1,'2024-01-06 15:00:00','2024-01-06 15:00:00'),(7,'wujiu','wujiu@email.com','13800138007','encrypted_pwd_7','吴九','avatar7.jpg',1,'1994-11-05',1,'2024-01-07 16:00:00','2024-01-07 16:00:00'),(8,'zhengshi','zhengshi@email.com','13800138008','encrypted_pwd_8','郑十','avatar8.jpg',2,'1996-02-14',1,'2024-01-08 17:00:00','2024-01-08 17:00:00'),(9,'qianyi','qianyi@email.com','13800138009','encrypted_pwd_9','钱一','avatar9.jpg',1,'1997-09-30',1,'2024-01-09 18:00:00','2024-01-09 18:00:00'),(10,'suner','suner@email.com','13800138010','encrypted_pwd_10','孙二','avatar10.jpg',2,'1998-04-12',1,'2024-01-10 19:00:00','2024-01-10 19:00:00'),(11,'lisan','lisan@email.com','13800138011','encrypted_pwd_11','李三','avatar11.jpg',1,'1999-06-08',1,'2024-01-11 20:00:00','2024-01-11 20:00:00'),(12,'wangsi','wangsi@email.com','13800138012','encrypted_pwd_12','王四','avatar12.jpg',2,'2000-10-17',1,'2024-01-12 21:00:00','2024-01-12 21:00:00'),(13,'zhaowu','zhaowu@email.com','13800138013','encrypted_pwd_13','赵五','avatar13.jpg',1,'2001-12-25',1,'2024-01-13 22:00:00','2024-01-13 22:00:00'),(14,'qianliu','qianliu@email.com','13800138014','encrypted_pwd_14','钱六','avatar14.jpg',2,'2002-03-03',1,'2024-01-14 23:00:00','2024-01-14 23:00:00'),(15,'sunqi2','sunqi2@email.com','13800138015','encrypted_pwd_15','孙七','avatar15.jpg',1,'2003-05-19',1,'2024-01-15 09:00:00','2024-01-15 09:00:00'),(16,'liba','liba@email.com','13800138016','encrypted_pwd_16','李八','avatar16.jpg',2,'2004-07-07',1,'2024-01-16 10:00:00','2024-01-16 10:00:00'),(17,'wangjiu','wangjiu@email.com','13800138017','encrypted_pwd_17','王九','avatar17.jpg',1,'2005-08-28',1,'2024-01-17 11:00:00','2024-01-17 11:00:00'),(18,'zhoushi','zhoushi@email.com','13800138018','encrypted_pwd_18','周十','avatar18.jpg',2,'2006-01-11',1,'2024-01-18 12:00:00','2024-01-18 12:00:00'),(19,'wushi','wushi@email.com','13800138019','encrypted_pwd_19','吴十','avatar19.jpg',1,'2007-02-23',1,'2024-01-19 13:00:00','2024-01-19 13:00:00'),(20,'zhengyi','zhengyi@email.com','13800138020','encrypted_pwd_20','郑一','avatar20.jpg',2,'2008-04-04',1,'2024-01-20 14:00:00','2024-01-20 14:00:00');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-28 15:32:01
