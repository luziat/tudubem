package org.example.tudubem.world;

// verticesJson 예시: "[[120,80],[220,80],[220,180],[120,180]]" (픽셀 좌표 기준 x,y 쌍 3개 이상)
public record DynamicObjectRequest(
        String objectId,
        String verticesJson
) {
}
