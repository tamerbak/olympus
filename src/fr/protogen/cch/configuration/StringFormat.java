package fr.protogen.cch.configuration;

public class StringFormat {
	private static StringFormat instance = null;
	
	public static synchronized StringFormat getInstance(){
		if(instance ==null)
			instance = new StringFormat();
		return instance;
	}
	
	public String tableDataReferenceFormat(String table){
		String dataReference="user_";
		for(int i = 0 ; i < table.length() ; i++){
			char c = table.charAt(i);
			switch(c){
				case 'é':case 'è':case 'ê': c='e';break;
				case 'à':c='a';break;
				case 'ç':c='c';break;
				case 'ô':c='o';break;
				case ' ':case '\t':c='_';break;
				case '/':case'-':case':':case';':c='_';break;
			}
			dataReference = dataReference+c;
		}
		
		return dataReference.toLowerCase();
		
	}

	public String attributeDataReferenceFormat(String attributeTitle) {
		// TODO Auto-generated method stub
		String dataReference="";
		for(int i = 0 ; i < attributeTitle.length() ; i++){
			char c = attributeTitle.charAt(i);
			switch(c){
				case 'é':case 'è':case 'ê': c='e';break;
				case 'à':c='a';break;
				case 'ç':c='c';break;
				case 'ô':c='o';break;
				case ' ':case '\t':case '\'':c='_';break;
				case '/':case ';':c='_';break;
			}
			dataReference = dataReference+c;
		}
		
		return dataReference.toLowerCase();
	}

	public String parameterFormat(String label) {
		// TODO Auto-generated method stub
		String dataReference="";
		
		if(label==null)
			return "";
		
		for(int i = 0 ; i < label.length() ; i++){
			char c = label.charAt(i);
			
			switch(c){
				case 'é':case 'è':case 'ê': c='e';break;
				case 'à':c='a';break;
				case 'ç':c='c';break;
				case 'ô':c='o';break;
				case ' ':case '\t':case '\'':c='_';break;
				case '/':case ';':c='_';break;
			}
			dataReference = dataReference+c;
		}
		
		return dataReference.toLowerCase();
	}

	public String formatQuery(String dataReference) {
		// TODO Auto-generated method stub
		if(dataReference == null)
			return "";
		String result = dataReference.replaceAll("'", "''");
		return result;
	}
}
