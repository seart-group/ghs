-- MySQL dump 10.13  Distrib 8.0.22, for Linux (x86_64)
--
-- Host: localhost    Database: gse
-- ------------------------------------------------------
-- Server version	8.0.22

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `access_token`
--

DROP TABLE IF EXISTS `access_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `value` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `added` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_token` (`value`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `crawl_job`
--

DROP TABLE IF EXISTS `crawl_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `crawl_job` (
  `crawl_id` bigint NOT NULL,
  `crawled` datetime DEFAULT NULL,
  `language_id` bigint NOT NULL,
  PRIMARY KEY (`crawl_id`,`language_id`),
  KEY `crawl_job_index_language_id` (`language_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL,
  `version` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `description` varchar(200) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `type` varchar(20) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `script` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `checksum` int DEFAULT NULL,
  `installed_by` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `execution_time` int NOT NULL,
  `success` tinyint(1) NOT NULL,
  PRIMARY KEY (`installed_rank`),
  KEY `flyway_schema_history_s_idx` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `hibernate_sequence`
--

DROP TABLE IF EXISTS `hibernate_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hibernate_sequence` (
  `next_val` bigint DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `repo`
--

DROP TABLE IF EXISTS `repo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repo` (
  `id` bigint NOT NULL,
  `name` varchar(140) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_fork_project` bit(1) DEFAULT NULL,
  `commits` bigint DEFAULT NULL,
  `branches` bigint DEFAULT NULL,
  `default_branch` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `releases` bigint DEFAULT NULL,
  `contributors` bigint DEFAULT NULL,
  `license` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `watchers` bigint DEFAULT NULL,
  `stargazers` bigint DEFAULT NULL,
  `forks` bigint DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `pushed_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `homepage` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `main_language` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `total_issues` bigint DEFAULT NULL,
  `open_issues` bigint DEFAULT NULL,
  `total_pull_requests` bigint DEFAULT NULL,
  `open_pull_requests` bigint DEFAULT NULL,
  `last_commit` timestamp NULL DEFAULT NULL,
  `last_commit_sha` varchar(40) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `has_wiki` bit(1) DEFAULT NULL,
  `archived` bit(1) DEFAULT NULL,
  `crawled` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_repo_name` (`name`),
  KEY `id_index` (`id`),
  KEY `search_col_index` (`name`,`main_language`,`license`,`commits`,`contributors`,`total_issues`,`open_issues`,`total_pull_requests`,`open_pull_requests`,`branches`,`releases`,`stargazers`,`watchers`,`forks`,`created_at`,`pushed_at`),
  KEY `main_lang_index` (`main_language`),
  KEY `bool_col_index` (`has_wiki`,`is_fork_project`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `repo_label`
--

DROP TABLE IF EXISTS `repo_label`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repo_label` (
  `repo_label_id` bigint NOT NULL,
  `repo_id` bigint NOT NULL,
  `repo_label_name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `crawled` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`repo_label_id`,`repo_id`),
  KEY `repo_label_index_repo_id` (`repo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `repo_language`
--

DROP TABLE IF EXISTS `repo_language`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repo_language` (
  `repo_language_id` bigint NOT NULL,
  `repo_id` bigint NOT NULL,
  `repo_language_name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `size_of_code` bigint NOT NULL,
  `crawled` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`repo_language_id`,`repo_id`),
  KEY `repo_language_index_repo_id` (`repo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supported_language`
--

DROP TABLE IF EXISTS `supported_language`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supported_language` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `added` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_language` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2021-01-22 18:02:45
