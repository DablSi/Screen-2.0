package com.example.ducks.screen;


import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.LinkedList;

public interface Service {

    //Добавить девайс
    @Multipart
    @POST("/Server-0.0.2-SNAPSHOT/post")
    public Call<Integer> putDevice(@Part("device") String device, @Part("room") Integer room, @Part("date") Long date);

    //Добавить координаты
    //Camera
    @Multipart
    @POST("/Server-0.0.2-SNAPSHOT/post/coords")
    public Call<Void> putCoords(@Part("room") int room, @Part("x1") double x1, @Part("y1") double y1, @Part("x2") double x2, @Part("y2") double y2, @Part("color") int[] color);

    //Получить координаты
    //Video
    @GET("/Server-0.0.2-SNAPSHOT/get/coords/{device}")
    public Call<Coords> getCoords(@Path("device") String device);

    //Получить время запуска
    //Search +
    @GET("/Server-0.0.2-SNAPSHOT/get/{device}")
    public Call<Long> getTime(@Path("device") String device);
    
    //Получить цвет
    //Search +
    @GET("/Server-0.0.2-SNAPSHOT/get/color/{device}")
    public Call<int[]> getColor(@Path("device") String device);

    //Получение номера комнаты
    //Camera +
    @GET("/Server-0.0.2-SNAPSHOT/get/room")
    public Call<Integer> getRoom();

    @GET(value = "/Server-0.0.2-SNAPSHOT/download/{room}")
    @Streaming
    public Call<ResponseBody> getFile(@Path("room") int room);

    @Multipart
    @POST(value = "/Server-0.0.2-SNAPSHOT/upload")
    public Call<Void> uploadVideo(@Part MultipartBody.Part video, @Part("room") int room);

    //Получение массива цветов
    @GET("/Server-0.0.2-SNAPSHOT/get/colors")
    public Call<int[]> getColors();

    //Добавить время запуска видео
    @Multipart
    @POST("/Server-0.0.2-SNAPSHOT/post/startVideo")
    public Call<Void> putStartVideo(@Part("room") Integer room, @Part("date") Long date);

    //Получить время запуска видео
    @GET("/Server-0.0.2-SNAPSHOT/get/startVideo/{device}")
    public Call<Long> getStartVideo(@Path("device") String device);

    //Получение индексов цветов
    @GET("/Server-0.0.2-SNAPSHOT/get/indexes/{room}")
    public Call<int[]> getIndexes(@Path("room") int room);

    //Поставить паузу || Продолжить воспроизведение
    @Multipart
    @POST("/Server-0.0.2-SNAPSHOT/post/pause")
    public Call<Void> setPause(@Part("room") int room, @Part("pause") boolean pause);

    //Получение массива цветов
    @GET("/Server-0.0.2-SNAPSHOT/get/pause/{room}")
    public Call<Boolean> getPause(@Path("room") int room);

    //Данные каждого гаджета
    class DeviceData {
        public Integer color, room;
        public Coords coords;

        public DeviceData(int newRoom) {
            room = newRoom;
        }
    }

    //Данные каждой комнаты
    class RoomData {
        public LinkedList<String> deviceList;
        public Long time;
        public byte[] video;

        public RoomData() {
            deviceList = new LinkedList<>();
        }
    }

    //Класс координат
    class Coords {
        public double x1, y1, x2, y2;

        public Coords(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }
    }


}
