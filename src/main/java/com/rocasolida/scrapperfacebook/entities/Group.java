package com.rocasolida.scrapperfacebook.entities;

import java.util.List;

import lombok.Data;

@Data
public class Group {
	private String description;
	private int cantMembers;
	private int cantPubsPorDia;
	private int cantPubsUltimosDias;
	private Long dateCreationUTime;
	//Si es privado o abierto
	private boolean lock;
	private List<GroupPublication> publications;
	//se puede extraer(lock):
			// Descripcion
			//div[@class='_j1y']//descendant::div[@role='heading']/p/span
			//miembros
			//div[@class='_4bl7']/div/div (CAntidad)
			//ACtividad (publicaciones nuevas hoy y en los ultimos dias)
			//div[@class='_4bl7']/div/div/following-sibling::div//following-sibling::div
			//Hace cuanto fue creado.(FEcha de creación. UTIME)
			//div[@class='_ifv']//descendant::abbr (getAttribute data-utime)
			//Es cerrado o abierto por el candadito
			//i[@class='_3ph1 img sp_wgvnRQRltZf sx_9d1f94']
}
