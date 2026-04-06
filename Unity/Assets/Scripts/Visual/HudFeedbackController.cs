using PeterDrummer.Core;
using PeterDrummer.Data;
using UnityEngine;
using UnityEngine.UI;

namespace PeterDrummer.Visual
{
    /// <summary>
    /// Atualiza textos de score/combo/judgement em HUD simples.
    /// </summary>
    public class HudFeedbackController : MonoBehaviour
    {
        [SerializeField] private ScoreSystem scoreSystem;
        [SerializeField] private Text scoreText;
        [SerializeField] private Text comboText;
        [SerializeField] private Text judgementText;

        private void Update()
        {
            if (scoreSystem == null) return;

            if (scoreText != null) scoreText.text = $"Score: {scoreSystem.Score}";
            if (comboText != null) comboText.text = $"Combo: {scoreSystem.Combo}";

            if (judgementText != null)
            {
                string err = scoreSystem.LastErrorMs < 0d ? "--" : $"{scoreSystem.LastErrorMs:0.0}ms";
                judgementText.text = $"{scoreSystem.LastJudgement} ({err})";
                judgementText.color = scoreSystem.LastJudgement switch
                {
                    HitJudgement.Perfect => Color.cyan,
                    HitJudgement.Good => Color.green,
                    _ => Color.red
                };
            }
        }
    }
}
