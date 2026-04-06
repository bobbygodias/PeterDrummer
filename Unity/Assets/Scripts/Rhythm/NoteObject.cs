using PeterDrummer.Data;
using PeterDrummer.Core;
using UnityEngine;

namespace PeterDrummer.Rhythm
{
    public class NoteObject : MonoBehaviour
    {
        public DrumLane Lane { get; private set; }
        public double TargetSongTimeSec { get; private set; }

        private SongConductor _conductor;
        private double _spawnDspTime;
        private float _spawnX;
        private float _hitX;
        private float _travelTimeSec;

        public void Initialize(
            DrumLane lane,
            double targetSongTimeSec,
            SongConductor conductor,
            double spawnDspTime,
            float spawnX,
            float hitX,
            float scrollSpeed)
        {
            Lane = lane;
            TargetSongTimeSec = targetSongTimeSec;
            _conductor = conductor;
            _spawnDspTime = spawnDspTime;
            _spawnX = spawnX;
            _hitX = hitX;
            _travelTimeSec = Mathf.Abs(spawnX - hitX) / scrollSpeed;
        }

        private void Update()
        {
            if (_conductor == null || !_conductor.IsPlaying) return;

            double elapsed = AudioSettings.dspTime - _spawnDspTime;
            float alpha = Mathf.Clamp01((float)(elapsed / _travelTimeSec));
            float x = Mathf.Lerp(_spawnX, _hitX, alpha);

            Vector3 pos = transform.position;
            pos.x = x;
            transform.position = pos;
        }
    }
}
