package be.javerage.cmee;

/**
 * Created by Elsen on 27/12/2014.
 */
public class User{

    private int _userid = -1;
    private String _username = "Guest";
    private int _score = 0;
    private int _scoreWeek = 0;
    private int _scoreDay = 0;

    public void SetUserid(int Userid){_userid = Userid;}
    public int GetUserid(){return _userid;}

    public String GetUsername() { return _username;}
    public void SetUsername(String _username) {this._username = _username;}

    public void AddScore(int points){
        _score += points; _scoreDay += points; _scoreWeek+= points;
    }
    public int GetScore(){
        return _score;
    }
    public int GetScoreWeek(){
        return _scoreWeek;
    }
    public int GetScoreDay(){
        return _scoreDay;
    }

    public void ClearScoreWeek(){
        _scoreWeek = 0;
    }
    public void ClearScoreDay(){
        _scoreDay = 0;
    }

    public void SetScore(int score, int scoreWeek,int scoreDay){
         _score = score;  _scoreWeek = scoreWeek; _scoreDay = scoreDay;
    }
}
