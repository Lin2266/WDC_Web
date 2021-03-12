package course.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//spring所提供的關聯注入di可分2種:2.JAVA-based:使用JAVA類別搭配annotation設定類別間的關聯性。
@Configuration	//宣告為spring的設定類別
public class SoldierConfig {
	@Bean	//表示spring啟動時將執行該方法，並將產出的物件納管為bean元件
	public WeaponDI getWeapon() {
		return new GunDI();
	}
	
	@Bean
	public Soldier getSoldier() {
		return new MySoldierDI(getWeapon());
	}
}
