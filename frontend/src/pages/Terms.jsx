import theme from "../styles/theme";
import { useNavigate } from "react-router-dom";

function Terms() {
  const navigate = useNavigate();

  const sectionStyle = {
    marginBottom: "20px",
    fontFamily: "'Arial', sans-serif",
    lineHeight: "1.6",
    color: theme.textPrimary,
    fontSize: "14px",
  };

  const headingStyle = {
    fontFamily: "'Georgia', serif",
    color: theme.textPrimary,
    marginTop: "30px",
    marginBottom: "10px",
    fontSize: "18px",
  };

  return (
    <div style={{
      backgroundColor: theme.backgroundWarm,
      minHeight: "calc(100vh - 64px)",
      padding: "30px 20px",
    }}>
        <button
           onClick={() => navigate(-1)}
           style={{
             position: "fixed",
             top: "84px",
             left: "20px",
             background: theme.backgroundWhite,
             border: `1px solid ${theme.border}`,
             color: theme.textPrimary,
             padding: "10px 14px",
             fontSize: "12px",
             letterSpacing: "0.08em",
             textTransform: "uppercase",
             cursor: "pointer",
             borderRadius: "2px",
             zIndex: 90,
           }}
         >
           ← Back
      </button>
      <div style={{
        maxWidth: "900px",
        margin: "0 auto",
        backgroundColor: theme.backgroundWhite,
        padding: "30px",
        borderRadius: "12px",
        border: `1px solid ${theme.border}`,
        boxShadow: "0 2px 8px rgba(0,0,0,0.06)",
      }}>
        <h1 style={{
          fontFamily: "'Georgia', serif",
          borderBottom: `2px solid ${theme.textAccent}`,
          paddingBottom: "10px",
        }}>
          Terms & Conditions
        </h1>

        <p style={sectionStyle}><strong>Last updated:</strong> April 3, 2026</p>

        <p style={sectionStyle}>
          We are Limerick Liquor LTD ('Company', 'we', 'us', or 'our'), a company registered in Ireland at Ardvarna, Lisnagry, Castleconnell, Limerick V94 DPN0. Our VAT number is LL99999999999901. We operate the website http://www.limerickliquor.com, as well as any other related products and services that refer or link to these legal terms.
        </p>
        <p style={sectionStyle}>
          You can contact us by phone at 0838325683, email at limerickliquor@gmail.com, or by mail to Ardvarna, Lisnagry, Castleconnell, Limerick V94 DPN0, Ireland.
        </p>
        <p style={sectionStyle}>
          By accessing the Services, you have read, understood, and agreed to be bound by all of these Legal Terms. IF YOU DO NOT AGREE WITH ALL OF THESE LEGAL TERMS, THEN YOU ARE EXPRESSLY PROHIBITED FROM USING THE SERVICES AND YOU MUST DISCONTINUE USE IMMEDIATELY.
        </p>

        <h2 style={headingStyle}>1. Our Services</h2>
        <p style={sectionStyle}>
          The information provided when using the Services is not intended for distribution to or use by any person or entity in any jurisdiction or country where such distribution or use would be contrary to law or regulation. Those who choose to access the Services from other locations do so on their own initiative and are solely responsible for compliance with local laws.
        </p>

        <h2 style={headingStyle}>2. Intellectual Property Rights</h2>
        <p style={sectionStyle}>
          We are the owner or the licensee of all intellectual property rights in our Services, including all source code, databases, functionality, software, website designs, audio, video, text, photographs, and graphics (collectively, the 'Content'), as well as the trademarks, service marks, and logos contained therein (the 'Marks'). Our Content and Marks are protected by copyright and trademark laws and treaties around the world.
        </p>
        <p style={sectionStyle}>
          Subject to your compliance with these Legal Terms, we grant you a non-exclusive, non-transferable, revocable licence to access the Services and download or print a copy of any portion of the Content solely for your personal, non-commercial use. Any other use requires our express prior written permission — contact limerickliquor@gmail.com.
        </p>

        <h2 style={headingStyle}>3. User Representations</h2>
        <p style={sectionStyle}>By using the Services, you represent and warrant that:</p>
        <ul style={sectionStyle}>
          <li>All registration information you submit will be true, accurate, current, and complete</li>
          <li>You have the legal capacity and agree to comply with these Legal Terms</li>
          <li>You are not a minor in the jurisdiction in which you reside</li>
          <li>You will not access the Services through automated or non-human means</li>
          <li>You will not use the Services for any illegal or unauthorised purpose</li>
          <li>Your use of the Services will not violate any applicable law or regulation</li>
        </ul>

        <h2 style={headingStyle}>4. User Registration</h2>
        <p style={sectionStyle}>
          You may be required to register to use the Services. You agree to keep your password confidential and will be responsible for all use of your account and password. We reserve the right to remove, reclaim, or change a username you select if we determine it is inappropriate, obscene, or otherwise objectionable.
        </p>

        <h2 style={headingStyle}>5. Products</h2>
        <p style={sectionStyle}>
          We make every effort to display products accurately, but we do not guarantee that colours, features, specifications, and details will be entirely accurate. All products are subject to availability, and we cannot guarantee that items will be in stock. We reserve the right to discontinue any products at any time. Prices are subject to change.
        </p>

        <h2 style={headingStyle}>6. Purchases and Payment</h2>
        <p style={sectionStyle}>We accept the following forms of payment: Visa, Mastercard, and American Express.</p>
        <p style={sectionStyle}>
          All payments shall be in Euros. You agree to provide current, complete, and accurate purchase and account information for all purchases. We may change prices at any time and reserve the right to correct any errors in pricing, refuse any order, or limit quantities purchased per person, per household, or per order.
        </p>

        <h2 style={headingStyle}>7. Return Policy</h2>
        <p style={sectionStyle}>All sales are final and no refund will be issued.</p>

        <h2 style={headingStyle}>8. Prohibited Activities</h2>
        <p style={sectionStyle}>You agree not to:</p>
        <ul style={sectionStyle}>
          <li>Systematically retrieve data to create collections or databases without written permission</li>
          <li>Trick, defraud, or mislead us or other users</li>
          <li>Circumvent, disable, or interfere with security-related features of the Services</li>
          <li>Upload or transmit viruses, Trojan horses, or other harmful material</li>
          <li>Engage in any automated use of the system, including bots or scrapers</li>
          <li>Attempt to impersonate another user or person</li>
          <li>Interfere with or create an undue burden on the Services</li>
          <li>Use the Services for any illegal or unauthorised purpose</li>
          <li>Use the Services to advertise or offer to sell goods and services</li>
          <li>Sell or otherwise transfer your profile</li>
        </ul>

        <h2 style={headingStyle}>9. User Generated Contributions</h2>
        <p style={sectionStyle}>
          Where the Services allow you to create or submit content, you warrant that your Contributions are accurate, not misleading, not in violation of any third party rights, and comply with all applicable laws. You are solely responsible for your Contributions.
        </p>

        <h2 style={headingStyle}>10. Contribution Licence</h2>
        <p style={sectionStyle}>
          You retain full ownership of all of your Contributions and any associated intellectual property rights. By submitting suggestions or feedback, you agree that we can use and share such feedback for any purpose without compensation to you.
        </p>

        <h2 style={headingStyle}>11. Services Management</h2>
        <p style={sectionStyle}>
          We reserve the right to monitor the Services for violations, take appropriate legal action, restrict access to or disable any Contributions, and otherwise manage the Services to protect our rights and property.
        </p>

        <h2 style={headingStyle}>12. Term and Termination</h2>
        <p style={sectionStyle}>
          These Legal Terms shall remain in full force and effect while you use the Services. We reserve the right to deny access, terminate your use, or delete your account at any time, without warning, at our sole discretion. If your account is terminated, you are prohibited from registering a new account.
        </p>

        <h2 style={headingStyle}>13. Modifications and Interruptions</h2>
        <p style={sectionStyle}>
          We reserve the right to change, modify, or remove the contents of the Services at any time without notice. We cannot guarantee the Services will be available at all times and will not be liable for any loss caused by downtime or discontinuance.
        </p>

        <h2 style={headingStyle}>14. Governing Law</h2>
        <p style={sectionStyle}>
          These Legal Terms are governed by and interpreted following the laws of Ireland. If your habitual residence is in the EU and you are a consumer, you additionally possess the protection provided by obligatory provisions of the law in your country of residence.
        </p>

        <h2 style={headingStyle}>15. Dispute Resolution</h2>
        <p style={sectionStyle}>
          The Parties agree to first attempt to negotiate any Dispute informally for at least thirty (30) days before initiating arbitration. Any dispute arising from these Legal Terms shall be determined by one arbitrator in accordance with the rules of the European Court of Arbitration. The seat of arbitration shall be Limerick, Ireland. Proceedings shall be in English under the law of Ireland.
        </p>

        <h2 style={headingStyle}>16. Corrections</h2>
        <p style={sectionStyle}>
          There may be information on the Services that contains typographical errors, inaccuracies, or omissions. We reserve the right to correct any errors and update information at any time without prior notice.
        </p>

        <h2 style={headingStyle}>17. Disclaimer</h2>
        <p style={sectionStyle}>
          THE SERVICES ARE PROVIDED ON AN AS-IS AND AS-AVAILABLE BASIS WITHOUT WARRANTIES OF ANY KIND. TO THE FULLEST EXTENT PERMITTED BY LAW, WE DISCLAIM ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT.
        </p>

        <h2 style={headingStyle}>18. Limitations of Liability</h2>
        <p style={sectionStyle}>
          IN NO EVENT WILL WE OR OUR DIRECTORS, EMPLOYEES, OR AGENTS BE LIABLE TO YOU OR ANY THIRD PARTY FOR ANY DIRECT, INDIRECT, CONSEQUENTIAL, EXEMPLARY, INCIDENTAL, SPECIAL, OR PUNITIVE DAMAGES, INCLUDING LOST PROFIT, LOST REVENUE, OR LOSS OF DATA ARISING FROM YOUR USE OF THE SERVICES.
        </p>

        <h2 style={headingStyle}>19. Indemnification</h2>
        <p style={sectionStyle}>
          You agree to defend, indemnify, and hold us harmless from and against any loss, damage, liability, or claim arising out of your use of the Services, breach of these Legal Terms, violation of any third party's rights, or any harmful act toward other users.
        </p>

        <h2 style={headingStyle}>20. User Data</h2>
        <p style={sectionStyle}>
          We will maintain certain data you transmit to the Services for the purpose of managing performance. Although we perform regular backups, you are solely responsible for all data you transmit. We shall have no liability for any loss or corruption of such data.
        </p>

        <h2 style={headingStyle}>21. Electronic Communications</h2>
        <p style={sectionStyle}>
          Visiting the Services, sending us emails, and completing online forms constitute electronic communications. You consent to receive electronic communications and agree that all agreements and notices provided electronically satisfy any legal requirement that such communication be in writing.
        </p>

        <h2 style={headingStyle}>22. Miscellaneous</h2>
        <p style={sectionStyle}>
          These Legal Terms constitute the entire agreement between you and us. Our failure to exercise any right or provision shall not operate as a waiver. If any provision is found unlawful or unenforceable, the remaining provisions remain in full effect. There is no joint venture, partnership, employment, or agency relationship created between you and us as a result of these Legal Terms.
        </p>

        <h2 style={headingStyle}>23. Contact Us</h2>
        <p style={sectionStyle}>
          Limerick Liquor LTD<br />
          Ardvarna, Lisnagry<br />
          Castleconnell, Limerick V94 DPN0<br />
          Ireland<br />
          Phone: 0838325683<br />
          Email: limerickliquor@gmail.com
        </p>

        <p style={{ ...sectionStyle, marginTop: "30px", fontSize: "12px", color: theme.textMuted }}>
          This is a demo project. These Terms & Conditions are provided for demonstration purposes only. Terms were created using Termly
        </p>
      </div>
    </div>
  );
}

export default Terms;