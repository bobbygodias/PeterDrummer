using PeterDrummer.Data;
using PeterDrummer.InputSystem;
using UnityEngine;

namespace PeterDrummer.Core
{
    /// <summary>
    /// Sistema simples de score/combos para primeira versão testável.
    /// </summary>
    public class ScoreSystem : MonoBehaviour
    {
        [SerializeField] private PlayerInputController inputController;

        public int Score { get; private set; }
        public int Combo { get; private set; }
        public int BestCombo { get; private set; }

        public HitJudgement LastJudgement { get; private set; }
        public double LastErrorMs { get; private set; }

        private void OnEnable()
        {
            inputController.OnJudgement += OnJudgement;
        }

        private void OnDisable()
        {
            inputController.OnJudgement -= OnJudgement;
        }

        private void OnJudgement(DrumLane lane, HitJudgement judgement, double errorMs)
        {
            LastJudgement = judgement;
            LastErrorMs = errorMs;

            switch (judgement)
            {
                case HitJudgement.Perfect:
                    Combo++;
                    Score += 300 + Combo * 2;
                    break;

                case HitJudgement.Good:
                    Combo++;
                    Score += 150 + Combo;
                    break;

                case HitJudgement.Miss:
                    Combo = 0;
                    break;
            }

            if (Combo > BestCombo)
            {
                BestCombo = Combo;
            }
        }
    }
}
