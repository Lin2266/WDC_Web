package course.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SoldierMain {

	public static void main(String[] args) {
		//spring所提供的關聯注入di可分2種:1.XML-based:使用XML檔案設定類別間的關聯性
       		      		
		//使用ClassPath...並指定xml檔案Appli...xml建立spring的執行環境
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"/course/spring/ApplicationContext.xml");
		//利用class指定物件型態，自spring的執行環境取出soldier元件
		Soldier soldier = context.getBean(Soldier.class);
		//Soldier.class => course.spring.MySoldierDI@235ecd9f，
		//MySoldierDI實作Soldier的destroyTarget方法
		System.out.println(soldier);
		soldier.destroyTarget();

	}

}
