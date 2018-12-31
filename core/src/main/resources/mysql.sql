CREATE TABLE `chat`.`user` (
  `id` VARCHAR(20) NOT NULL,
  `nick_name` VARCHAR(45) NULL,
  `phone_no` VARCHAR(20) NULL,
  `sex` INT NULL,
  `city` VARCHAR(45) NULL,
  `password` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `phone_no_UNIQUE` (`phone_no` ASC));



CREATE TABLE `chat`.`friend` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `master_id` VARCHAR(20) NOT NULL,
  `friend_id` VARCHAR(20) NOT NULL,
  `custom_name` VARCHAR(45) NULL,
  PRIMARY KEY (`id`));

ALTER TABLE `chat`.`friend`
ADD CONSTRAINT `fk`
  FOREIGN KEY (`master_id`)
  REFERENCES `chat`.`user` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;

ALTER TABLE `chat`.`friend`
DROP INDEX `fk_idx` ,
ADD INDEX `fk_idx` (`master_id` ASC),
ADD INDEX `fk_idx1` (`friend_id` ASC);
ALTER TABLE `chat`.`friend`
ADD CONSTRAINT `fks`
  FOREIGN KEY (`friend_id`)
  REFERENCES `chat`.`user` (`id`)
  ON DELETE CASCADE
  ON UPDATE CASCADE;
