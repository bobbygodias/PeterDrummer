using System;

namespace PeterDrummer.Data
{
    public enum DrumLane
    {
        Kick = 0,
        Snare = 1,
        HiHat = 2
    }

    [Serializable]
    public struct BeatEvent
    {
        public DrumLane Lane;
        public double TimeSec;
        public float Strength;

        public BeatEvent(DrumLane lane, double timeSec, float strength)
        {
            Lane = lane;
            TimeSec = timeSec;
            Strength = strength;
        }
    }
}
