package be.khlim.student.cmee.cmee;

/**
 * Created by Elsen on 27/12/2014.
 */
public class User{

    private int _score;
    private int _userid = -1;
    public void SetUserid(int Userid){_userid = Userid;}
    public int GetUserid(){return _userid;}

    public void AddScore(int points){
        _score += points;
    }

    public int GetScore(){
        return _score;
    }
    public void SetScore(int score){
         _score = score;
    }
}
