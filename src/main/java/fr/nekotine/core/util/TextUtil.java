package fr.nekotine.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import fr.nekotine.core.tuple.Pair;

public class TextUtil {
	public static HashMap<String, List<Integer>> rabinKarp(String text, String... patterns){
		HashMap<String, List<Integer>> found = new HashMap<String, List<Integer>>();
		HashMap<String, Integer> subHashValue = new HashMap<String, Integer>();
		
		if(patterns.length == 0)
			return found;
		
		int n = text.length();
		int minLength = -1;
		
		//Get minimum pattern length
		for(String pattern : patterns) {
			int patternLength = pattern.length();
			if(minLength <0 || minLength>patternLength) minLength = patternLength;
		}
		
		//Calculate sub hash value of the patterns
		for(String pattern : patterns) {
			if(pattern.length() > n)
				continue;
			
			int patternHash = 0;
			for(int i=0 ; i < minLength ; i++)
				patternHash += pattern.charAt(i);
			subHashValue.put(pattern, patternHash);
		}
		
		//Calculate initial hash value
		int hashValue = 0;
		for(int i=0 ; i < minLength ; i++) {
			hashValue += text.charAt(i);
		}
		
		//Rabin Karp Algorithm
		for(int i=0 ; i < n - minLength + 1 ; i++) {
			
			for(String pattern : subHashValue.keySet()) {
				
				int patternLength = pattern.length();
				
				if(i + patternLength - 1 >= n) {
					continue;
				}
				
				if(subHashValue.get(pattern) == hashValue) {
					boolean patternFound = true;
					for(int j= 0 ; j < patternLength ; j++) {
						
						if(pattern.charAt(j) != text.charAt(i + j)) {
							patternFound = false;
							break;
						}
					}
					
					if(patternFound) {
						if(!found.containsKey(pattern)) 
							found.put(pattern, new ArrayList<Integer>());
						
						found.get(pattern).add(i);
					}
				}
			}
			
			if(i < n - minLength)
				hashValue += text.charAt(i + minLength) - text.charAt(i);
		}
		
		return found;
	}
	
	public static void rabinKarp(String text, Consumer<Pair<Integer,String>> action,String... patterns){
		HashMap<String, Integer> subHashValue = new HashMap<String, Integer>();
		
		if(patterns.length == 0)
			return;
		
		int n = text.length();
		int minLength = -1;
		
		//Get minimum pattern length
		for(String pattern : patterns) {
			int patternLength = pattern.length();
			if(minLength <0 || minLength>patternLength) minLength = patternLength;
		}
		
		//Calculate sub hash value of the patterns
		for(String pattern : patterns) {
			if(pattern.length() > n)
				continue;
			
			int patternHash = 0;
			for(int i=0 ; i < minLength ; i++)
				patternHash += pattern.charAt(i);
			subHashValue.put(pattern, patternHash);
		}
		
		//Calculate initial hash value
		int hashValue = 0;
		for(int i=0 ; i < minLength ; i++) {
			hashValue += text.charAt(i);
		}
		
		//Rabin Karp Algorithm
		for(int i=0 ; i < n - minLength + 1 ; i++) {
			
			for(String pattern : subHashValue.keySet()) {
				
				int patternLength = pattern.length();
				
				if(i + patternLength - 1 >= n) {
					continue;
				}
				
				if(subHashValue.get(pattern) == hashValue) {
					boolean patternFound = true;
					for(int j= 0 ; j < patternLength ; j++) {
						
						if(pattern.charAt(j) != text.charAt(i + j)) {
							patternFound = false;
							break;
						}
					}
					
					if(patternFound) {
						action.accept(Pair.from(i, pattern));
					}
				}
			}
			
			if(i < n - minLength)
				hashValue += text.charAt(i + minLength) - text.charAt(i);
		}
	}
	
	public static String insertText(String original, String toAdd, int position) {
		StringBuilder builder = new StringBuilder(original);
		return builder.insert(position, toAdd).toString();
	}
}
