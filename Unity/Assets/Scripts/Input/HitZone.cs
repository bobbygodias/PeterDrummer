using System.Collections.Generic;
using PeterDrummer.Data;
using PeterDrummer.Rhythm;
using UnityEngine;

namespace PeterDrummer.InputSystem
{
    public class HitZone : MonoBehaviour
    {
        [SerializeField] private DrumLane lane;
        private readonly List<NoteObject> _inside = new();

        public DrumLane Lane => lane;

        public NoteObject GetClosest(double songTimeSec)
        {
            NoteObject best = null;
            double bestAbs = double.MaxValue;

            foreach (NoteObject note in _inside)
            {
                if (note == null) continue;
                double err = System.Math.Abs(note.TargetSongTimeSec - songTimeSec);
                if (err < bestAbs)
                {
                    bestAbs = err;
                    best = note;
                }
            }
            return best;
        }

        private void OnTriggerEnter2D(Collider2D other)
        {
            if (other.TryGetComponent(out NoteObject note) && note.Lane == lane)
            {
                _inside.Add(note);
            }
        }

        private void OnTriggerExit2D(Collider2D other)
        {
            if (other.TryGetComponent(out NoteObject note))
            {
                _inside.Remove(note);
            }
        }

        public void Remove(NoteObject note) => _inside.Remove(note);
    }
}
