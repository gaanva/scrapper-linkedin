package com.rocasolida.scrapperfacebook.entities;

import lombok.Data;

public @Data class Reaction {
	//"Me gusta"; "Me encanta"; "Me divierte"; "Me asombra"; "Me Entristese"; "ME enfada"
	String type;
	int cant;
}
