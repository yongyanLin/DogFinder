package com.example.dogfinder.Utils;


import com.example.dogfinder.Entity.Behavior;
import com.example.dogfinder.Entity.Body;
import com.example.dogfinder.Entity.Size;
import com.example.dogfinder.R;

import java.util.ArrayList;
import java.util.List;

public class DataUtil {
    public static List<Body> getBodyList(){
        List<Body> list = new ArrayList<>();
        Body b = new Body();
        b.setDescription("");
        b.setName("Condition");
        b.setImage(0);
        list.add(b);
        Body b1 = new Body();
        b1.setDescription("Ribs and backbone easily seen with no overlying fat layer.Exaggerated waistline.Severe tummy tuck.");
        b1.setName("Very thin");
        b1.setImage(R.mipmap.verythin);
        list.add(b1);
        Body b2 = new Body();
        b2.setDescription("Ribs, backbone and hip bones easily seen1; with no overlying fat layer Some mid muscle loss particularly noticeable around shoulders and thighs");
        b2.setName("Thin");
        b2.setImage(R.mipmap.thin);
        list.add(b2);
        Body b3 = new Body();
        b3.setDescription("Ribs can be easily felt and may/may not be seen1 with minimal layer of overlying fat. A clear waistline can easily be seen. Noticeable tummy tuck");
        b3.setName("Ideal");
        b3.setImage(R.mipmap.ideal);
        list.add(b3);
        Body b4 = new Body();
        b4.setDescription("Ribs can be felt, but generally can't be seen, with a obvious layer of overlaying fat. Waistline is visible, but not clear.Tummy tucks slightly upwards towards back legs");
        b4.setName("Overweight");
        b4.setImage(R.mipmap.overweight);
        list.add(b4);
        Body b5 = new Body();
        b5.setDescription("Ribs are very difficult to feel under a very thick layer of overlying fat. Chunky pads of fat can be felt along the back and at the base of the tail.Waistline is absent");
        b5.setName("Obese");
        b5.setImage(R.mipmap.obese);
        list.add(b5);
        return list;
    }
    public static List<Behavior> getBehaviorList(){
        List<Behavior> list = new ArrayList<>();
        Behavior b = new Behavior();
        b.setName("Behavior");
        b.setDescription("Please choose the similar behavior of this stray dog.");
        list.add(b);
        Behavior b1 = new Behavior();
        b1.setName("Fearful");
        b1.setDescription("The strange sounds and sights may encourage them to be defensive and fearful. The fear may manifest as aggression, even when approached by a well-meaning stranger. ");
        list.add(b1);
        Behavior b2 = new Behavior();
        b2.setName("Aloof");
        b2.setDescription("They typically avoid strangers, running away when approached and will only overcome their fear when hunger gets the better of them.");
        list.add(b2);
        Behavior b3 = new Behavior();
        b3.setName("Friendly");
        b3.setDescription("Such dogs will happily approach strangers, other dogs and will not run away if approached. ");
        list.add(b3);
        Behavior b4 = new Behavior();
        b4.setName("Aggressive");
        b4.setDescription("Being aggressive due to the surroundings, they may exhibit aggression toward other dogs or humans.");
        list.add(b4);

        return list;
    }
    public static List<Size> getSizeList(){
        List<Size> list = new ArrayList<>();
        Size s = new Size();
        s.setName("Size");
        list.add(s);
        Size s1 = new Size();
        s1.setName("Small");
        s1.setImage(R.mipmap.small_size);
        list.add(s1);
        Size s2 = new Size();
        s2.setName("Medium");
        s2.setImage(R.mipmap.medium_size);
        list.add(s2);
        Size s3 = new Size();
        s3.setName("Large");
        s3.setImage(R.mipmap.large_size);
        list.add(s3);
        Size s4 = new Size();
        s4.setName("Giant");
        s4.setImage(R.mipmap.giant_size);
        list.add(s4);

        return list;
    }
    public static List<String> getLocationOption(){
        List<String> list = new ArrayList<>();
        list.add("Distance");
        list.add("Any distance");
        list.add("10 miles");
        list.add("20 miles");
        list.add("50 miles");
        list.add("100 miles");
        list.add("200 miles");
        return list;
    }
    public static List<String> getTimeOption(){
        List<String> list = new ArrayList<>();
        list.add("Post Time");
        list.add("Any time");
        list.add("Within a week");
        list.add("Within a month");
        list.add("Within three months");
        list.add("Within six months");
        return list;
    }
    public static String[] getColorArray() {
        String colors[] = {"Apricot/Beige","White","Bicolor", "Black", "Brindle", "Brown/Chocolate", "Golden",
                "Gray/Blue/Silver", "Harlequin", "Blue", "Red"};
        return colors;
    }

    public static double distance(double lat1,double lon1,double lat2,double lon2){
        double diffLon = lon1-lon2;
        //calculate distance
        double distance = Math.sin(degrad(lat1))
                *Math.sin(degrad(lat2))
                +Math.cos(degrad(lat1))
                *Math.cos(degrad(lat2))
                *Math.cos(degrad(diffLon));
        distance = Math.acos(distance);
        //distance in miles
        distance = raddeg(distance);
        distance = distance * 60 * 1.1515;
        //in kilometers
        //distance = distance * 1.609344;
        //keep two decimal
        distance = Math.round(distance * 100.0) / 100.0;
        return distance;
    }
    //convert degree to radian
    public static double degrad(double lat1){
        return (lat1*Math.PI/180.0);
    }
    public static double raddeg(double distance){
        return (distance * 180.0 / Math.PI);
    }

}
