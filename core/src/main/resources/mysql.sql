CREATE TABLE `chat`.`user` (
  `id` VARCHAR(20) NOT NULL,
  `nick_name` VARCHAR(45) NULL,
  `phone_no` VARCHAR(20) NULL,
  `sex` INT NULL,
  `city` VARCHAR(45) NULL,
  `password` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `phone_no_UNIQUE` (`phone_no` ASC));